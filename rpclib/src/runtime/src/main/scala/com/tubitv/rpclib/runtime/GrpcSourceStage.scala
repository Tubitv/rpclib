package com.tubitv.rpclib.runtime

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, OutHandler}
import io.grpc.stub.{CallStreamObserver, StreamObserver}

import scala.concurrent.{Future, Promise}

class GrpcSourceStage[Req, Resp](requestStream: CallStreamObserver[Resp])
  extends GraphStageWithMaterializedValue[SourceShape[Req], Future[StreamObserver[Req]]] {
  val out = Outlet[Req]("grpc.out")
  override val shape: SourceShape[Req] = SourceShape.of(out)

  override def createLogicAndMaterializedValue(
                                                inheritedAttributes: Attributes
                                              ): (GraphStageLogic, Future[StreamObserver[Req]]) = {
    val promise: Promise[StreamObserver[Req]] = Promise()

    val logic = new GraphStageLogic(shape) with OutHandler {
      val inObs = new StreamObserver[Req] {
        override def onError(t: Throwable) =
          getAsyncCallback((t: Throwable) => fail(out, t)).invoke(t)

        override def onCompleted() =
          getAsyncCallback((_: Unit) => complete(out)).invoke(())

        override def onNext(value: Req) =
          getAsyncCallback((value: Req) => push(out, value)).invoke(value)
      }

      override def onPull(): Unit = requestStream.request(1)

      override def preStart(): Unit = {
        requestStream.disableAutoInboundFlowControl()
        promise.success(inObs)
      }

      setHandler(out, this)
    }

    (logic, promise.future)
  }
}
