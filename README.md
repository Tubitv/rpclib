#  RPCLib

This is a library that enables gRPC services and clients written in
Scala to seamlessly use gRPC methods as though they were Akka Stream
flows.  Given a Protocol Buffer such as the following:

```proto
service ExampleService {
  rpc Unary           (       Request) returns (       Response);
  rpc ServerStreaming (       Request) returns (stream Response);
  rpc ClientStreaming (stream Request) returns (       Response);
  rpc BidiStreaming   (stream Request) returns (stream Response);
}
```

This library enables Scala services and clients to use the following
uniform API:

```scala
trait ExampleService {
  def unary           : Flow[Request, Response, NotUsed]
  def serverStreaming : Flow[Request, Response, NotUsed]
  def clientStreaming : Flow[Request, Response, NotUsed]
  def bidiStreaming   : Flow[Request, Response, NotUsed]
}
```

##  Quick start

To start using RPCLib in your project:

  1.  Add the RPCLib plugin to SBT by adding the following to
      `project/plugins.sbt`:

          addSbtPlugin("com.tubitv.rpclib" % "rpclib-compiler" % "0.3.0")
          addSbtPlugin("com.thesamet"      % "sbt-protoc"      % "0.99.18")

  2.  Tell RPCLib where your Protocol Buffer files are located by adding
      the following lines to your project definition (e.g., in
      `build.sbt`), substituting `"<path to protobuf directory>"` with a
      directory path as a string (e.g., `"src/main/protos"`):

          import com.tubitv.rpclib.compiler.RpcLibCodeGenerator

          PB.protoSources in Compile := Seq(
            baseDirectory.value / "<path to protobuf directory>"
          ),

          PB.targets in Compile := Seq(
            scalapb.gen()       -> (sourceManaged in Compile).value,
            RpcLibCodeGenerator -> (sourceManaged in Compile).value
          )

If your project isn't already using ScalaPB, you'll also need to add two
files, `scalapb/scalapb.proto` and `google/protobuf/descriptor.proto`,
to your Protocol Buffer directory.  See the example projects in the
`/examples/minimal-server/` and `/examples/minimal-client/` directories
for these files.

##  Quick start for RPCLib developers

Contributions to RPCLib are very welcome!  Please open pull requests.

This repository is organized into three directories:

  *  `/rpclib/`: sources for RPCLib;

  *  `/rpclib-test/`: tests for RPCLib; and

  *  `/examples/`: example projects demonstrating usage of RPCLib.

###  Source files (`/rpclib/`)

The SBT project for RPCLib consists of two subprojects:

  *  `rpclib-compiler`: an SBT plugin that generates code at compile
     time from Protocol Buffers; and,

  *  `rpclib-runtime`, a Scala runtime library used by the generated
     code.

To publish the library locally, execute the following in the `/rpclib/`
directory:

    $ sbt +publishLocal

This will compile both subprojects and publish the resulting artifacts
to your local machine.

###  Tests (`/rpclib-test/`)

To run the library tests, execute the following in the `/rpclib-test/`
directory:

    $ sbt test
