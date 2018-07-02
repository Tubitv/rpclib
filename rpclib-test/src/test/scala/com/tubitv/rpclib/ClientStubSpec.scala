package com.tubitv.rpclib

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.util.Random

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.tubitv.rpc.Request
import com.tubitv.rpc.TestService
import com.tubitv.rpc.TestService.Methods._
import com.tubitv.rpclib.runtime.FailurePolicy.RetryPolicy.BackoffStrategy.Exponential
import com.tubitv.rpclib.runtime.FailurePolicy._
import com.tubitv.rpclib.runtime.headers._
import com.tubitv.rpclib.testkit.ClientTestEnvironment

/** Specification for client stubs.
 *
 *  These test cases are organized along three dimensions:
 *
 *    1.  The method type (unary, server streaming, client streaming, and
 *        bidirectional streaming)
 *
 *    2.  The failure policy (circuit breaker, deadline, and retry policies) and
 *        tracing headers
 *
 *    3.  Different ways of instantiating a stub
 */
// TODO(dan): See `https://docs.google.com/document/d/1HV6nkU2vNPUe_-dRqN-A7T0lGpLk1qLtiHI6Op_RJFI/preview`
class ClientStubSpec extends TestKit(ActorSystem("ClientStubSpec"))
                        with WordSpecLike
                        with Matchers
                        with ClientTestEnvironment {

  import ClientStubSpec._

  implicit val streamMaterializer = ActorMaterializer()(system)

  val `x-request-id`      = `X-Request-Id`(UUID.randomUUID().toString)
  val `x-b3-traceid`      = `X-B3-TraceId`("abc")
  val `x-b3-spanid`       = `X-B3-SpanId`("def")
  val `x-b3-parentspanid` = `X-B3-ParentSpanId`("ghi")
  val `x-ot-span-context` = `X-Ot-Span-Context`("jkl")

  implicit val grpcHeaders = Seq(
    `x-request-id`,
    `x-b3-traceid`,
    `x-b3-spanid`,
    `x-b3-parentspanid`,
    `x-ot-span-context`
  )

  // TODO(dan): The test cases below are very repetitive.  See if they can be
  // simplified by using a ScalaTest table.

  "A client stub using out-of-the-box defaults" when {

    val service = TestService.stub(this.channel)

    "calling a unary method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Request(Random.nextInt)
      val serviceMethod = service.unary

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual ???
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual ???
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual ???
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request.head shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a server streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Request(Random.nextInt)
      val serviceMethod = service.serverStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual ???
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual ???
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual ???
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request.head shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a client streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Seq.fill(3)(Request(Random.nextInt))
      val serviceMethod = service.clientStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual ???
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual ???
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual ???
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a bidirectional streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Seq.fill(3)(Request(Random.nextInt))
      val serviceMethod = service.bidiStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual ???
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual ???
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual ???
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }
  }

  "A client stub using user-specified, stub-wide defaults" when {

    val service =
      TestService.stub(this.channel)
        .withDefaults(
          circuitBreaker = CircuitBreakerPolicies.Default,
          deadline = DeadlinePolicies.Default,
          retries = RetryPolicies.Default
        )

    "calling a unary method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Request(Random.nextInt)
      val serviceMethod = service.unary

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.Default
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.Default
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.Default
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request.head shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a server streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Request(Random.nextInt)
      val serviceMethod = service.serverStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.Default
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.Default
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.Default
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request.head shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a client streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Seq.fill(3)(Request(Random.nextInt))
      val serviceMethod = service.clientStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.Default
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.Default
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.Default
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a bidirectional streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Seq.fill(3)(Request(Random.nextInt))
      val serviceMethod = service.bidiStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.Default
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.Default
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.Default
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }
  }

  "A client stub using user-specified, per-method defaults" when {

    val service =
      TestService.stub(this.channel)
        .withDefaults(
          circuitBreaker = CircuitBreakerPolicies.Default,
          deadline = DeadlinePolicies.Default,
          retries = RetryPolicies.Default
        )
        .withOverridesFor(Unary)(
          circuitBreaker = CircuitBreakerPolicies.Unary,
          deadline = DeadlinePolicies.Unary,
          retries = RetryPolicies.Unary
        )
        .withOverridesFor(ServerStreaming)(
          circuitBreaker = CircuitBreakerPolicies.ServerStreaming,
          deadline = DeadlinePolicies.ServerStreaming,
          retries = RetryPolicies.ServerStreaming
        )
        .withOverridesFor(ClientStreaming)(
          circuitBreaker = CircuitBreakerPolicies.ClientStreaming,
          deadline = DeadlinePolicies.ClientStreaming,
          retries = RetryPolicies.ClientStreaming
        )
        .withOverridesFor(BidiStreaming)(
          circuitBreaker = CircuitBreakerPolicies.BidiStreaming,
          deadline = DeadlinePolicies.BidiStreaming,
          retries = RetryPolicies.BidiStreaming
        )

    "calling a unary method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Request(Random.nextInt)
      val serviceMethod = service.unary

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.Unary
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.Unary
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.Unary
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request.head shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a server streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Request(Random.nextInt)
      val serviceMethod = service.serverStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.ServerStreaming
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.ServerStreaming
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.ServerStreaming
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request.head shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a client streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Seq.fill(3)(Request(Random.nextInt))
      val serviceMethod = service.clientStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.ClientStreaming
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.ClientStreaming
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.ClientStreaming
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }

    "calling a bidirectional streaming method" should {

      // TODO(dan): Replace this with ScalaCheck
      val req = Seq.fill(3)(Request(Random.nextInt))
      val serviceMethod = service.bidiStreaming

      "set tracing headers" in {
        (req ~> serviceMethod) {
          // TODO(dan): Add syntactic sugar to allow `Option(`x-request-id`)` (note the lack of `.value`)
          header[`X-Request-Id`]        shouldEqual Option(`x-request-id`.value)
          header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`.value)
          header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`.value)
          header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`.value)
          header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`.value)
          header[`X-Client-Trace-Id`]   shouldEqual None
          header[`X-Envoy-Force-Trace`] shouldEqual None
          header[`X-B3-Sampled`]        shouldEqual None
          header[`X-B3-Flags`]          shouldEqual None
        }
      }

      "have the correct circuit breaker policy" in {
        pending
        (req ~> serviceMethod) {
          circuitBreakerPolicy shouldEqual CircuitBreakerPolicies.BidiStreaming
        }
      }

      "have the correct deadline policy" in {
        pending
        (req ~> serviceMethod) {
          deadlinePolicy shouldEqual DeadlinePolicies.BidiStreaming
        }
      }

      "have the correct retry policy" in {
        pending
        (req ~> serviceMethod) {
          retryPolicy shouldEqual RetryPolicies.BidiStreaming
        }
      }

      "deliver the request to the server" in {
        (req ~> serviceMethod) {
          request shouldEqual req
        }
      }

      "have default fallback behavior" in {
        pending
        (req ~> serviceMethod) {
        }
      }
    }
  }
}

object ClientStubSpec {

  // TODO(dan): Replace the following with ScalaCheck
  object CircuitBreakerPolicies {
    final val Default         = randPolicy()
    final val Unary           = randPolicy()
    final val ServerStreaming = randPolicy()
    final val ClientStreaming = randPolicy()
    final val BidiStreaming   = randPolicy()

    private final val MinFailures = 1
    private final val MaxFailures = 10
    private final val MinCooldownSecs = 1
    private final val MaxCooldownSecs = 60

    private def randPolicy(): CircuitBreakerPolicy = {
      CircuitBreakerPolicy(
        failures = nextInt(MinFailures, MaxFailures),
        cooldown = nextInt(MinCooldownSecs, MaxCooldownSecs).seconds
      )
    }
  }

  // TODO(dan): Replace the following with ScalaCheck
  object DeadlinePolicies {
    final val Default         = randPolicy()
    final val Unary           = randPolicy()
    final val ServerStreaming = randPolicy()
    final val ClientStreaming = randPolicy()
    final val BidiStreaming   = randPolicy()

    private final val MinDeadlineMillis = 1
    private final val MaxDeadlineMillis = 5000

    private def randPolicy(): DeadlinePolicy =
      DeadlinePolicy(nextInt(MinDeadlineMillis, MaxDeadlineMillis).millis)
  }

  // TODO(dan): Replace the following with ScalaCheck
  object RetryPolicies {
    final val Default         = randPolicy()
    final val Unary           = randPolicy()
    final val ServerStreaming = randPolicy()
    final val ClientStreaming = randPolicy()
    final val BidiStreaming   = randPolicy()

    private final val MinTries = 1
    private final val MaxTries = 10
    private final val MinHalfLifeMillis = 10
    private final val MaxHalfLifeMillis = 5000

    private def randPolicy(): RetryPolicy = {
      RetryPolicy(
        tries = nextInt(MinTries, MaxTries),
        backoff = Exponential(nextInt(MinHalfLifeMillis, MaxHalfLifeMillis).millis)
      )
    }
  }
}
