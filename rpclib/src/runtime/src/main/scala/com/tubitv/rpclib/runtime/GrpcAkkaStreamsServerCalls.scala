package com.tubitv.rpclib.runtime

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tubitv.rpclib.runtime.headers.EnvoyHeaders
import com.tubitv.rpclib.runtime.interceptors.EnvoyHeadersServerInterceptor
import io.grpc.{Context, ServerCallHandler}
import io.grpc.stub.{CallStreamObserver, ServerCalls, StreamObserver}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object GrpcAkkaStreamsServerCalls {

  def unaryCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncUnaryCall(
      new ServerCalls.UnaryMethod[Req, Resp] {
        override def invoke(request: Req, responseObserver: StreamObserver[Resp]) =
          Source
            .single(request)
            .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
            .runForeach(responseObserver.onNext)
            .onComplete {
              case Success(_) => responseObserver.onCompleted()
              case Failure(err) => responseObserver.onError(err)
            }(mat.executionContext)
      }
    )

  def serverStreamingCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncServerStreamingCall(
      new ServerCalls.ServerStreamingMethod[Req, Resp] {
        override def invoke(request: Req, responseObserver: StreamObserver[Resp]): Unit =
          Source
            .single(request)
            .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
            .runWith(Sink.fromGraph(new GrpcSinkStage[Resp](
              responseObserver.asInstanceOf[CallStreamObserver[Resp]]
            )))
      }
    )

  def clientStreamingCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncClientStreamingCall(
      new ServerCalls.ClientStreamingMethod[Req, Resp] {
        override def invoke(responseObserver: StreamObserver[Resp]): StreamObserver[Req] =
        // blocks until the GraphStage is fully initialized
          Await.result(
            Source
              .fromGraph(new GrpcSourceStage[Req, Resp](
                responseObserver.asInstanceOf[CallStreamObserver[Resp]]
              ))
              .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
              .to(Sink.fromGraph(new GrpcSinkStage[Resp](
                responseObserver.asInstanceOf[CallStreamObserver[Resp]]
              ))).run(),
            Duration.Inf
          )
      }
    )

  def bidiStreamingCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncBidiStreamingCall(
      new ServerCalls.BidiStreamingMethod[Req, Resp] {
        override def invoke(responseObserver: StreamObserver[Resp]): StreamObserver[Req] =
        // blocks until the GraphStage is fully initialized
          Await.result(
            Source
              .fromGraph(new GrpcSourceStage[Req, Resp](
                responseObserver.asInstanceOf[CallStreamObserver[Resp]]
              ))
              .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
              .to(Sink.fromGraph(new GrpcSinkStage[Resp](
                responseObserver.asInstanceOf[CallStreamObserver[Resp]]
              ))).run(),
            Duration.Inf
          )
      }
    )
}
