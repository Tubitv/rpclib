package com.tubitv.rpclib.testkit.dsl

import scala.collection.immutable.Iterable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._

import akka.Done
import io.grpc.stub.StreamObserver

import com.tubitv.rpc.Request
import com.tubitv.rpc.Response
import com.tubitv.rpclib.testkit.TestEnvironment
import com.tubitv.rpclib.testkit.spies.ResponseSpy

/** Trait containing a convenience DSL. */
private[testkit] trait ServerTestDSL extends TestEnvironment with ResponseSpy {

  implicit class TestDSLForSingleRequests(request: Request)(implicit timeout: FiniteDuration = 1.second) {
    def ~>[T](method: Request => Future[Response])(check: => T): T = {
      responseSpy = Seq(Await.result(method(request), timeout))
      check
    }

    def ~>[T](method: (Request, StreamObserver[Response]) => Unit)(check: => T): T = {
      val responseBuffer = ListBuffer.empty[Response]
      val donePromise = Promise[Done]
      val done = donePromise.future

      method(
        request,
        new StreamObserver[Response] {
          override def onError(t: Throwable): Unit = ()
          override def onCompleted(): Unit = {
            responseSpy = responseBuffer.toList
            donePromise.success(Done)
          }
          override def onNext(response: Response): Unit = {
            responseBuffer += response
          }
        }
      )

      Await.ready(done, timeout)
      check
    }
  }

  implicit class TestDSLForIterables(request: Iterable[Request])(implicit timeout: FiniteDuration = 1.second) {
    def ~>[T](method: StreamObserver[Response] => StreamObserver[Request])(check: => T): T = {
      val responseBuffer = ListBuffer.empty[Response]
      val donePromise = Promise[Done]
      val done = donePromise.future

      val requestObserver = method(
        new StreamObserver[Response] {
          override def onError(t: Throwable): Unit = ()
          override def onCompleted(): Unit = {
            responseSpy = responseBuffer.toList
            donePromise.success(Done)
          }
          override def onNext(response: Response): Unit = {
            responseBuffer += response
          }
        }
      )

      request.foreach(requestObserver.onNext)
      requestObserver.onCompleted()

      Await.ready(done, timeout)
      check
    }
  }
}
