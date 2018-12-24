package com.tubitv.rpclib.runtime

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tubitv.rpclib.runtime.headers.EnvoyHeaders
import com.tubitv.rpclib.runtime.interceptors.EnvoyHeadersServerInterceptor
import io.grpc.stub.ServerCalls.BidiStreamingMethod
import io.grpc.stub.{CallStreamObserver, ServerCalls, StreamObserver}
import io.grpc._

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
              case Success(_) =>
                responseObserver.onCompleted()
              case Failure(s: StatusException) =>
                responseObserver.onError(s)
              case Failure(s: StatusRuntimeException) =>
                responseObserver.onError(s)
              case Failure(e) =>
                responseObserver.onError(
                  Status.INTERNAL.withDescription(e.getMessage).withCause(e).asException()
                )
            }(mat.executionContext)
      }
    )

  def serverStreamingCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncServerStreamingCall(
      new ServerCalls.ServerStreamingMethod[Req, Resp] {
        def invoke (request: Req, responseObserver: StreamObserver[Resp]) =
          Source
            .single(request)
            .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
            .runWith(Sink.fromGraph(new GrpcSinkStage[Resp](
              responseObserver.asInstanceOf[CallStreamObserver[Resp]]
            )))
      })

  def clientStreamingCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncClientStreamingCall(new ServerCalls.ClientStreamingMethod[Req, Resp] {
      override def invoke(responseObserver: StreamObserver[Resp]) = {
        val callStreamObserver = responseObserver.asInstanceOf[CallStreamObserver[Resp]]
        Await.result(
          Source
            .fromGraph(new GrpcSourceStage[Req, Resp](callStreamObserver))
            .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
            .to(Sink.fromGraph(new GrpcSinkStage[Resp](callStreamObserver))
            ).run(),
          Duration.Inf
        )
      }
    })

  def bidiStreamingCall[Req, Resp, NotUsed](getService: EnvoyHeaders => Flow[Req, Resp, NotUsed])(
    implicit mat: Materializer
  ): ServerCallHandler[Req, Resp] =
    ServerCalls.asyncBidiStreamingCall(new BidiStreamingMethod[Req, Resp] {
      override def invoke(responseObserver: StreamObserver[Resp]) = {
        val callStreamObserver = responseObserver.asInstanceOf[CallStreamObserver[Resp]]
        Await.result(
          Source
            .fromGraph(new GrpcSourceStage[Req, Resp](callStreamObserver))
            .via(getService(EnvoyHeadersServerInterceptor.ContextKey.get(Context.current)))
            .to(Sink.fromGraph(new GrpcSinkStage[Resp](callStreamObserver))).run(),
          Duration.Inf
        )
      }
    })
}
