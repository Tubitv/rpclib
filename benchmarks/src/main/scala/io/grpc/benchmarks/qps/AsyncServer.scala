package io.grpc.benchmarks.qps

import java.util.NoSuchElementException
import java.util.concurrent.ForkJoinPool

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.Flow
import com.google.protobuf.ByteString
import com.tubitv.rpclib.runtime.headers.EnvoyHeaders
import io.grpc.{ServerServiceDefinition, Status}
import io.grpc.benchmarks.Utils
import io.grpc.benchmarks.proto.messages
import io.grpc.benchmarks.proto.messages.{Payload, SimpleRequest, SimpleResponse}
import io.grpc.benchmarks.proto.services.BenchmarkService
import io.grpc.netty.NettyServerBuilder
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultThreadFactory

object AsyncServer  {
  // FROM grpc-java
  // Always use the same canned response for bidi. This is allowed by the spec.
  val BIDI_RESPONSE_BYTES = 100
  val BIDI_RESPONSE = SimpleResponse(payload = Some(messages.Payload(body = ByteString.copyFrom(new Array[Byte](BIDI_RESPONSE_BYTES)))))

  def main(args: Array[String]): Unit = {

    val tf = new DefaultThreadFactory("server-elg-", true)
    val bossEventLoopGroup = new NioEventLoopGroup(1, tf)
    val workerEventLoopGroup = new NioEventLoopGroup(0, tf)

    implicit val actorSystem = ActorSystem()
    implicit val mat = ActorMaterializer()

    val service: ServerServiceDefinition = BenchmarkService.bindService(new AsyncServer())
    val server = NettyServerBuilder
      .forPort(50051)
      .bossEventLoopGroup(bossEventLoopGroup)
      .workerEventLoopGroup(workerEventLoopGroup)
      .channelType(classOf[NioServerSocketChannel])
      .executor(actorSystem.dispatcher)
      .addService(service)
      .build()
      .start()

    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      server.shutdownNow()
      System.err.println("*** server shut down")
    }

    System.out.println("QPS Server started")
    server.awaitTermination()
  }
}

class AsyncServer extends BenchmarkService.BenchmarkService {
  override def unaryCall(implicit headers: EnvoyHeaders): Flow[SimpleRequest, SimpleResponse, NotUsed] = {
    Flow[SimpleRequest]
      .map(Utils.makeResponse)
  }

  override def streamingCall(implicit headers: EnvoyHeaders): Flow[SimpleRequest, SimpleResponse, NotUsed] = {
    Flow[SimpleRequest]
      .map(Utils.makeResponse)
  }

  // grpc-java implementation simply emits a response based on the last element but throws if not element was ever sent
  // before the stream completed
  override def streamingFromClient(implicit headers: EnvoyHeaders): Flow[SimpleRequest, SimpleResponse, NotUsed] = {
    Flow[SimpleRequest]
      .reduce((acc, el) => el)
      .map(Utils.makeResponse)
      .mapError {
        case _: NoSuchElementException => Status.FAILED_PRECONDITION.withDescription("never received any requests").asException()
        case t: Throwable => t
      }
  }

  override def streamingFromServer(implicit headers: EnvoyHeaders): Flow[SimpleRequest, SimpleResponse, NotUsed] = {
    Flow[SimpleRequest]
      .map(Utils.makeResponse)
      .expand[SimpleResponse](resp => Iterator.continually(resp))
  }

  // grpc_java repeats the same response
  override def streamingBothWays(implicit headers: EnvoyHeaders): Flow[SimpleRequest, SimpleResponse, NotUsed] = {
    Flow[SimpleRequest]
      .map(_ => AsyncServer.BIDI_RESPONSE)
  }
}
