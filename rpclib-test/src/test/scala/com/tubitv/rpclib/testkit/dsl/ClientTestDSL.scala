package com.tubitv.rpclib.testkit.dsl

import scala.collection.immutable.Iterable
import scala.concurrent.Await
import scala.concurrent.duration._

import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.trueaccord.scalapb.Message

import com.tubitv.rpclib.testkit.TestEnvironment

/** Trait containing a convenience DSL.
 *
 *  This DSL allows you to write:
 *
 *  {{{
 *  val request: Request = ...
 *  val flow: Flow[Request, Response, NotUsed] = ...
 *
 *  (request ~> flow) {
 *    // your assertion logic here
 *  }
 *  }}}
 *
 *  and:
 *
 *  {{{
 *  val request: scala.collection.immutable.Iterable[Request] = ...
 *  val flow: Flow[Request, Response, NotUsed] = ...
 *
 *  (request ~> flow) {
 *    // your assertion logic here
 *  }
 *  }}}
 */
private[testkit] trait ClientTestDSL extends TestEnvironment {

  implicit val streamMaterializer: Materializer

  implicit class TestDSLForSingleRequests[In <: Message[_], Out <: Message[_]](in: In) {
    def ~>[Mat, T]
      (flow: Flow[In, Out, Mat])
      (check: => T)
      (implicit timeout: FiniteDuration = 1.second)
    : T = {
      val done =
        Source.single(in)
          .via(flow)
          .runWith(Sink.ignore)

      Await.ready(done, timeout)
      check
    }
  }

  implicit class TestDSLForIterables[In <: Message[_], Out <: Message[_]](in: Iterable[In]) {
    def ~>[Mat, T]
      (flow: Flow[In, Out, Mat])
      (check: => T)
      (implicit timeout: FiniteDuration = 1.second)
    : T = {
      val done =
        Source.apply(in)
          .via(flow)
          .runWith(Sink.ignore)

      Await.ready(done, timeout)
      check
    }
  }
}
