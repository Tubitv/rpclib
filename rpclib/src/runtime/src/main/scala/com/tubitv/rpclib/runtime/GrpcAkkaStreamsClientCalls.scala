package com.tubitv.rpclib.runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import io.grpc.{ClientCall, Metadata, Status}
import io.grpc.stub.{ClientCallStreamObserver, ClientCalls, ClientResponseObserver}
import scalapb.grpc.Grpc

object GrpcAkkaStreamsClientCalls {

  def unaryFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] = {
    Flow[Req].flatMapConcat(req => {
      Source.fromFuture(
        Grpc.guavaFuture2ScalaFuture(
          ClientCalls.futureUnaryCall(call, req)
        )
      )
    })
  }

  def serverStreamingFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] =
    Flow.fromGraph(
      new GrpcGraphStage[Req, Resp](outputObserver => {
        val out = outputObserver.asInstanceOf[ClientResponseObserver[Req, Resp]]
        val in = new ClientCallStreamObserver[Req] {
          val halfClosed = new AtomicBoolean(false)
          val onReadyHandler = new AtomicReference[Option[Runnable]](None)
          val listener = new ClientCall.Listener[Resp] {
            override def onClose(status: Status, trailers: Metadata): Unit =
              status.getCode match {
                case Status.Code.OK => out.onCompleted()
                case _ => out.onError(status.asException(trailers))
              }
            override def onMessage(message: Resp): Unit =
              out.onNext(message)
            override def onReady(): Unit =
              onReadyHandler.get().foreach(_.run())
          }
          call.start(listener, new Metadata())

          override def cancel(message: String, cause: Throwable): Unit =
            call.cancel(message, cause)
          override def setOnReadyHandler(onReadyHandler: Runnable): Unit =
            this.onReadyHandler.set(Some(onReadyHandler))
          override def request(count: Int): Unit = call.request(count)
          override def disableAutoInboundFlowControl(): Unit = ()
          override def isReady: Boolean = !halfClosed.get() || call.isReady
          override def setMessageCompression(enable: Boolean): Unit =
            call.setMessageCompression(enable)
          override def onError(t: Throwable): Unit =
            call.cancel("Cancelled by client with StreamObserver.onError()", t)
          override def onCompleted(): Unit = ()
          override def onNext(request: Req): Unit = {
            call.sendMessage(request)
            halfClosed.set(true)
            call.halfClose()
          }
        }
        out.beforeStart(in)
        in
      })
    )

  def clientStreamingFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] =
    Flow.fromGraph(new GrpcGraphStage[Req, Resp](ClientCalls.asyncClientStreamingCall(call, _)))

  def bidiStreamingFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] =
    Flow.fromGraph(new GrpcGraphStage[Req, Resp](ClientCalls.asyncBidiStreamingCall(call, _)))
}
