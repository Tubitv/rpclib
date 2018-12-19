package com.tubitv.rpclib.compiler

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.Descriptors.MethodDescriptor
import com.google.protobuf.Descriptors.ServiceDescriptor
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import protocbridge.Artifact
import protocbridge.ProtocCodeGenerator
import scalapb.compiler.{GeneratorException, _}
import scalapb.compiler.StreamType.{Bidirectional, ClientStreaming, ServerStreaming, Unary}
import scalapb.compiler.Version.{scalapbVersion => ScalaPbVersion}
import scalapb.options.compiler.Scalapb

object RpcLibCodeGenerator extends ProtocCodeGenerator {
  /** TODO(dan): Scaladoc */
  override def run(input: Array[Byte]): Array[Byte] = {
    val registry = ExtensionRegistry.newInstance()
    Scalapb.registerAllExtensions(registry)
    val request = CodeGeneratorRequest.parseFrom(input, registry)
    val builder = CodeGeneratorResponse.newBuilder()
    val params = GeneratorParams() // we don't use these for anything here

    val filesByName: Map[String, FileDescriptor] = request
      .getProtoFileList
      .asScala
      .foldLeft(Map.empty[String, FileDescriptor])((acc, fdProto) => {
        val deps = fdProto.getDependencyList.asScala.map(acc)
        acc + (fdProto.getName -> FileDescriptor.buildFrom(fdProto, deps.toArray))
      })
    val implicits = new DescriptorImplicits(params, filesByName.values.toVector)

    request
      .getFileToGenerateList
      .asScala
      .foreach(name => {
        val fd = filesByName(name)
        val response = generateServices(fd, implicits)
        builder.addAllFile(response.asJava)
      })
    builder.build.toByteArray
  }

  def generateServices(file: FileDescriptor, implicits: DescriptorImplicits): Seq[CodeGeneratorResponse.File] = {
    import implicits._

    file.getServices.asScala.map { service =>
        val p    = new RpcLibCodeGenerator(service, implicits)
        val code = p.run()
        val b    = CodeGeneratorResponse.File.newBuilder()
        b.setName(file.scalaDirectory + "/" + service.objectName + "AkkaStream.scala")
        b.setContent(code)
        b.build()
      }.toList
  }

  /** Transitive library dependencies.
   *
   *  This is a list of library dependencies that users of RPCLib will need.
   *  Instead of requiring users to manually add these libraries in their SBT
   *  build definitions, we define them here, and the user's SBT build will
   *  automatically pick them up as transitive dependencies.
   */
  override val suggestedDependencies: Seq[Artifact] = {
    Seq(
      Artifact("com.thesamet.scalapb", "scalapb-runtime-grpc", ScalaPbVersion, crossVersion = true),
      Artifact("com.tubitv.rpclib"   , "rpclib-runtime"      , RpclibRuntimeVersion , crossVersion = true)
    )
  }
}

final private class RpcLibCodeGenerator(service: ServiceDescriptor, implicits: DescriptorImplicits) {
  import implicits._

  // Anatomy of this class:
  //
  //   *  Routines that generate code for method definitions are written as
  //      methods.
  //
  //   *  Routines that generate classes/traits/objects are written within a
  //      nested object.
  //
  // Adhering to this consistency keeps the structure of this file in sync with
  // that of the generated code, which is especially helpful when viewing this
  // file with code folding.

  def run(): String = {
    new FunctionalPrinter()
      .call(addPackageClause)
      .newline
      .call(addImportStatements)
      .newline
      .call(ParentObjectCodeGenerator.apply)
      .result
  }

  private def addPackageClause(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.add(s"package ${service.getFile.scalaPackageName}")
  }

  private def addImportStatements(printer: FunctionalPrinter): FunctionalPrinter = {
    val scalaImports =
      Seq(
        "scala.concurrent.Await",
        "scala.concurrent.duration._",
        "scala.util.Failure",
        "scala.util.Success"
      )

    val libraryImports =
      Seq(
        "akka.NotUsed",
        "akka.stream.Materializer",
        "akka.stream.scaladsl.Flow",
        "akka.stream.scaladsl.Sink",
        "akka.stream.scaladsl.Source",
        "com.google.protobuf.Descriptors",
        "scalapb.grpc.AbstractService",
        "scalapb.grpc.ConcreteProtoFileDescriptorSupplier",
        "scalapb.grpc.Grpc",
        "scalapb.grpc.Marshaller",
        "scalapb.grpc.ServiceCompanion",
        "com.tubitv.rpclib.runtime.FailurePolicy.CircuitBreakerPolicy",
        "com.tubitv.rpclib.runtime.FailurePolicy.DeadlinePolicy",
        "com.tubitv.rpclib.runtime.FailurePolicy.Fallthrough",
        "com.tubitv.rpclib.runtime.FailurePolicy.RetryPolicy",
        "com.tubitv.rpclib.runtime.{GrpcAkkaStreamsClientCalls, GrpcAkkaStreamsServerCalls}",
        "com.tubitv.rpclib.runtime.headers.EnvoyHeaders",
        "com.tubitv.rpclib.runtime.interceptors.EnvoyHeadersClientInterceptor",
        "com.tubitv.rpclib.runtime.interceptors.EnvoyHeadersServerInterceptor",
        "io.grpc.CallOptions",
        "io.grpc.Channel",
        "io.grpc.ClientInterceptors",
        "io.grpc.Context",
        "io.grpc.MethodDescriptor",
        "io.grpc.ServerInterceptors",
        "io.grpc.ServerServiceDefinition",
        "io.grpc.ServiceDescriptor",
        "io.grpc.stub.AbstractStub",
        "io.grpc.stub.ClientCalls",
        "io.grpc.stub.ServerCalls",
        "io.grpc.stub.StreamObserver"
      )

    printer
      .add(scalaImports.map(i => s"import ${i}"): _*)
      .newline
      .add(libraryImports.map(i => s"import ${i}"): _*)
  }

  private object ParentObjectCodeGenerator {

    def apply(printer: FunctionalPrinter): FunctionalPrinter = {
      printer
        .add(s"object ${service.name} {")
        .newline
        .indent
        .call(ServiceTraitCodeGenerator.apply)
        .newline
        .call(ClientStubCodeGenerator.apply)
        .newline
        .call(ReifiedMethodNamesCodeGenerator.apply)
        .newline
        .call(addPrivateMethodDescriptors)
        .call(addBindServiceMethod)
        .newline
        .call(addStubMethod)
        .newline
        .call(addJavaDescriptorMethod)
        .outdent
        .add("}")
    }

    private object ServiceTraitCodeGenerator {

      def apply(printer: FunctionalPrinter): FunctionalPrinter = {
        printer
          .call(TraitCodeGenerator.apply)
          .newline
          .call(CompanionObjectCodeGenerator.apply)
      }

      private object TraitCodeGenerator {
        def apply(printer: FunctionalPrinter): FunctionalPrinter = {
          printer
            .add(s"trait ${service.name} extends AbstractService {")
            .indent
            .newline
            .add(s"override def serviceCompanion = ${service.name}")
            .print(service.methods) { (printer, method) =>
              printer
                .newline
                .add(s"def ${method.name}(implicit headers: EnvoyHeaders):")
                .addIndented(s"Flow[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}, NotUsed]")
            }
            .outdent
            .add("}")
        }
      }

      private object CompanionObjectCodeGenerator {
        def apply(printer: FunctionalPrinter): FunctionalPrinter = {
          printer
            .add(s"object ${service.name} extends ServiceCompanion[${service.name}] {")
            .indent
            .add(s"implicit def serviceCompanion: ServiceCompanion[${service.name}] = this")
            .add("def javaDescriptor: Descriptors.ServiceDescriptor =")
            .addIndented(s"${service.getFile.fileDescriptorObjectFullName}.javaDescriptor.getServices.get(0)")
            .outdent
            .add("}")
        }
      }
    }

    private object ClientStubCodeGenerator {

      def apply(printer: FunctionalPrinter): FunctionalPrinter = {
        printer
          .call(ClassCodeGenerator.apply)
          .newline
          .call(CompanionObjectCodeGenerator.apply)
      }

      private object ClassCodeGenerator {

        def apply(printer: FunctionalPrinter): FunctionalPrinter = {
          printer
            .add(s"class ${service.name}Stub(channel: Channel, options: CallOptions = CallOptions.DEFAULT)")
            .addIndented(s"extends AbstractStub[${service.name}Stub](${service.name}Stub.addHeaders(channel), options) with ${service.name} {")
            .indent
            .newline
            .add(s"private val channelWithHeaders: Channel =")
            .addIndented(s"${service.name}Stub.addHeaders(channel)")
            .newline
            .call(addWithDefaultsMethod)
            .newline
            .call(addWithOverridesMethod)
            .print(service.methods)(addStubMethodImplementations)
            .newline
            .add(s"override def build(channel: Channel, options: CallOptions): ${service.name}Stub =")
            .addIndented(s"new ${service.name}Stub(channel, options)")
            .outdent
            .add("}")
        }

        private def addWithDefaultsMethod(printer: FunctionalPrinter): FunctionalPrinter = {
          printer
            .add("def withDefaults(")
            .addIndented(
              "circuitBreaker: CircuitBreakerPolicy = Fallthrough,",
              "deadline: DeadlinePolicy = Fallthrough,",
              "retries: RetryPolicy = Fallthrough"
            )
            .add(s"): ${service.name}Stub = {")
            .addIndented("this    // TODO: Implement this")
            .add("}")
        }

        private def addWithOverridesMethod(printer: FunctionalPrinter): FunctionalPrinter = {
          printer
            .add("def withOverridesFor(m: Method)(")
            .addIndented(
              "circuitBreaker: CircuitBreakerPolicy = Fallthrough,",
              "deadline: DeadlinePolicy = Fallthrough,",
              "retries: RetryPolicy = Fallthrough"
            )
            .add(s"): ${service.name}Stub = {")
            .addIndented("this    // TODO: Implement this")
            .add("}")
        }

        private def addStubMethodImplementations(printer: FunctionalPrinter, method: MethodDescriptor): FunctionalPrinter = {
          def addBody(printer: FunctionalPrinter): FunctionalPrinter = {
            method.streamType match {
              case Unary => printer
                .add(s"GrpcAkkaStreamsClientCalls.unaryFlow[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}](")
                .addIndented(s"() => channelWithHeaders.newCall(Method${method.getName}, options.withOption(EnvoyHeadersClientInterceptor.HeadersKey, envoyHeaders))")
                .add(")")
              case ServerStreaming => printer
                .add(s"GrpcAkkaStreamsClientCalls.serverStreamingFlow[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}](")
                .addIndented(s"() => channelWithHeaders.newCall(Method${method.getName}, options.withOption(EnvoyHeadersClientInterceptor.HeadersKey, envoyHeaders))")
                .add(")")
              case ClientStreaming => printer
                .add(s"GrpcAkkaStreamsClientCalls.clientStreamingFlow[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}](")
                .addIndented(s"() => channelWithHeaders.newCall(Method${method.getName}, options.withOption(EnvoyHeadersClientInterceptor.HeadersKey, envoyHeaders))")
                .add(")")
              case Bidirectional => printer
                .add(s"GrpcAkkaStreamsClientCalls.bidiStreamingFlow[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}](")
                .addIndented(s"() => channelWithHeaders.newCall(Method${method.getName}, options.withOption(EnvoyHeadersClientInterceptor.HeadersKey, envoyHeaders))")
                .add(")")
            }
          }

          printer
            .newline
            .add(s"override def ${method.name}(")
            .addIndented("implicit envoyHeaders: EnvoyHeaders")
            .add(s"): Flow[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}, NotUsed] = {")
            .indent
            .call(addBody)
            .outdent
            .add("}")
        }
      }

      private object CompanionObjectCodeGenerator {
        def apply(printer: FunctionalPrinter): FunctionalPrinter = {
          printer
            .add(s"object ${service.name}Stub {")
            .indent
            .add("private[this] final val HeadersInterceptor = new EnvoyHeadersClientInterceptor")
            .add("private def addHeaders(channel: Channel): Channel =")
            .addIndented("ClientInterceptors.intercept(channel, HeadersInterceptor)")
            .outdent
            .add("}")
        }
      }
    }

    private object ReifiedMethodNamesCodeGenerator {
      def apply(printer: FunctionalPrinter): FunctionalPrinter = {
        printer
          .add("sealed abstract class Method")
          .add("object Methods {")
          .indent
          .print(service.methods) { (printer, method) =>
            printer.add(s"case object ${method.getName} extends Method")
          }
          .outdent
          .add("}")
      }
    }

    private def addPrivateMethodDescriptors(printer: FunctionalPrinter): FunctionalPrinter = {
      def addMethodDescriptor(printer: FunctionalPrinter, method: MethodDescriptor): FunctionalPrinter = {
        val methodType = method.streamType match {
          case StreamType.Unary => "UNARY"
          case StreamType.ServerStreaming => "SERVER_STREAMING"
          case StreamType.ClientStreaming => "CLIENT_STREAMING"
          case StreamType.Bidirectional => "BIDI_STREAMING"
        }

        printer
          .add(s"private final val Method${method.getName}: MethodDescriptor[${method.inputType.baseScalaType}, ${method.outputType.baseScalaType}] = {")
          .indent
          .add("MethodDescriptor.newBuilder()")
          .addIndented(
            s".setType(MethodDescriptor.MethodType.${methodType})",
            s""".setFullMethodName(MethodDescriptor.generateFullMethodName("${method.getService.getFullName}", "${method.getName}"))""",
            s".setRequestMarshaller(new Marshaller(${method.inputType.baseScalaType}))",
            s".setResponseMarshaller(new Marshaller(${method.outputType.baseScalaType}))",
            ".build()"
          )
          .outdent
          .add("}")
          .newline
      }

      def addServiceDescriptor(printer: FunctionalPrinter): FunctionalPrinter = {
        printer
          .add("private final val Service: ServiceDescriptor = {")
          .indent
          .add(s"""ServiceDescriptor.newBuilder("${service.getFullName}")""")
          .indent
          .add(s".setSchemaDescriptor(new ConcreteProtoFileDescriptorSupplier(${service.getFile.fileDescriptorObjectFullName}.javaDescriptor))")
          .print(service.methods) { (printer, method) =>
            printer.add(s".addMethod(Method${method.getName})")
          }
          .add(".build()")
          .outdent
          .outdent
          .add("}")
      }

      printer
        .print(service.methods)(addMethodDescriptor)
        .call(addServiceDescriptor)
    }

    private def addBindServiceMethod(printer: FunctionalPrinter): FunctionalPrinter = {
      def addMethodCall(printer: FunctionalPrinter, method: MethodDescriptor): FunctionalPrinter = {
        def addBody(printer: FunctionalPrinter): FunctionalPrinter = {
          method.streamType match {
            case StreamType.Unary =>
              printer
                .add(s"Method${method.getName},")
                .add(s"GrpcAkkaStreamsServerCalls.unaryCall(serviceImpl.${method.name}(_))")
            case StreamType.ClientStreaming =>
              printer
                .add(s"Method${method.getName},")
                .add(s"GrpcAkkaStreamsServerCalls.clientStreamingCall(serviceImpl.${method.name}(_))")
            case StreamType.ServerStreaming =>
              printer
                .add(s"Method${method.getName},")
                .add(s"GrpcAkkaStreamsServerCalls.serverStreamingCall(serviceImpl.${method.name}(_))")
            case StreamType.Bidirectional =>
              printer
              .add(s"Method${method.getName},")
              .add(s"GrpcAkkaStreamsServerCalls.bidiStreamingCall(serviceImpl.${method.name}(_))")
          }
        }

        printer
          .add(".addMethod(")
          .indent
          .call(addBody)
          .outdent
          .add(")")
      }

      printer
        .newline
        .add(s"def bindService(serviceImpl: ${service.name})(implicit mat: Materializer): ServerServiceDefinition = {")
        .indent
        .add("ServerInterceptors.intercept(")
        .indent
        .add("ServerServiceDefinition.builder(Service)")
        .indent
        .print(service.methods)(addMethodCall)
        .add(".build(),")
        .outdent
        .add("new EnvoyHeadersServerInterceptor")
        .outdent
        .add(")")
        .outdent
        .add("}")
    }

    private def addStubMethod(printer: FunctionalPrinter): FunctionalPrinter = {
      printer.add(s"def stub(channel: Channel): ${service.name}Stub = new ${service.name}Stub(channel)")
    }

    private def addJavaDescriptorMethod(printer: FunctionalPrinter): FunctionalPrinter = {
      printer
        .add("def javaDescriptor: Descriptors.ServiceDescriptor =")
        .addIndented(s"${service.getFile.fileDescriptorObjectFullName}.javaDescriptor.getServices.get(0)")
    }
  }

  private implicit class RichFunctionalPrinter(printer: FunctionalPrinter) {
    def resultTrimmed: String = {
      printer
        .content
        .map(_.replaceAll("\\s+$", ""))
        .mkString("\n")
    }
  }
}
