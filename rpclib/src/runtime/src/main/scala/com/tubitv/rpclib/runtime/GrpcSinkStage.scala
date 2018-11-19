package com.tubitv.rpclib.runtime

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.grpc.stub.CallStreamObserver

class GrpcSinkStage[Resp](observer: CallStreamObserver[Resp]) extends GraphStage[SinkShape[Resp]] {
  val in = Inlet[Resp]("grpc.in")
  override val shape: SinkShape[Resp] = SinkShape.of(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler with Runnable {
      var element: Option[Resp] = None

      override def run(): Unit = getAsyncCallback((_: Unit) => {
        element match {
          case Some(value) if observer.isReady =>
            element = None
            observer.onNext(value)
            tryPull(in)
          case _ => ()
        }
      }).invoke(())

      override def onPush(): Unit = {
        val value = grab(in)
        if (observer.isReady) {
          observer.onNext(value)
          pull(in)
        } else element = Some(value)
      }

      override def onUpstreamFinish(): Unit = {
        element match {
          case Some(value) => observer.onNext(value)
          case _ =>
        }
        observer.onCompleted()
      }

      override def onUpstreamFailure(t: Throwable): Unit = observer.onError(t)

      override def preStart(): Unit = pull(in)

      observer.setOnReadyHandler(this)
      setHandler(in, this)
    }
}
