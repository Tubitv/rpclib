package com.tubitv.rpclib

import scala.collection.immutable.Seq

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.tubitv.rpc.Request
import com.tubitv.rpc.Response
import com.tubitv.rpclib.runtime.headers._
import com.tubitv.rpclib.runtime.interceptors.EnvoyHeadersClientInterceptor.HeadersKey
import com.tubitv.rpclib.testkit.ServerTestEnvironment

/** Specification for server implementations. */
class ServerSpec extends TestKit(ActorSystem("ServerSpec"))
                    with WordSpecLike
                    with Matchers
                    with ServerSpec.DependencyPreinitializer
                    with ServerTestEnvironment {

  import ServerSpec._

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

  // We will indirectly exercise the server by calling this client stub
  val service = this.clientStub.withOption(HeadersKey, grpcHeaders)

  "A unary method" should {

    // TODO(dan): Replace this with ScalaCheck
    val req = Request(nextInt(0, 1 << 16))
    val serviceMethod = service.unary _

    "set tracing headers" in {
      (req ~> serviceMethod) {
        header[`X-Request-Id`]        shouldEqual Option(`x-request-id`)
        header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`)
        header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`)
        header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`)
        header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`)
        header[`X-Client-Trace-Id`]   shouldEqual None
        header[`X-Envoy-Force-Trace`] shouldEqual None
        header[`X-B3-Sampled`]        shouldEqual None
        header[`X-B3-Flags`]          shouldEqual None
      }
    }

    "deliver the response to the client" in {
      (req ~> serviceMethod) {
        response.head shouldEqual Response(req.value + 1)
      }
    }
  }

  "A server streaming method" should {

    // TODO(dan): Replace this with ScalaCheck
    val req = Request(nextInt(1, 10))
    val serviceMethod = service.serverStreaming _

    "set tracing headers" in {
      (req ~> serviceMethod) {
        header[`X-Request-Id`]        shouldEqual Option(`x-request-id`)
        header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`)
        header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`)
        header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`)
        header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`)
        header[`X-Client-Trace-Id`]   shouldEqual None
        header[`X-Envoy-Force-Trace`] shouldEqual None
        header[`X-B3-Sampled`]        shouldEqual None
        header[`X-B3-Flags`]          shouldEqual None
      }
    }

    "deliver the response to the client" in {
      (req ~> serviceMethod) {
        response shouldEqual List.iterate(1, req.value)(_ + 1).map(Response.apply)
      }
    }
  }

  "A client streaming method" should {

    // TODO(dan): Replace this with ScalaCheck
    val req = Seq.fill(nextInt(1, 10))(Request(nextInt(0, 1 << 16)))
    val serviceMethod = service.clientStreaming _

    "set tracing headers" in {
      (req ~> serviceMethod) {
        header[`X-Request-Id`]        shouldEqual Option(`x-request-id`)
        header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`)
        header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`)
        header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`)
        header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`)
        header[`X-Client-Trace-Id`]   shouldEqual None
        header[`X-Envoy-Force-Trace`] shouldEqual None
        header[`X-B3-Sampled`]        shouldEqual None
        header[`X-B3-Flags`]          shouldEqual None
      }
    }

    "deliver the response to the client" in {
      (req ~> serviceMethod) {
        response.head shouldEqual Response(req.map(_.value).sum)
      }
    }
  }

  "A bidirectional streaming method" should {

    // TODO(dan): Replace this with ScalaCheck
    val req = Seq.fill(nextInt(1, 10))(Request(nextInt(0, 1 << 16)))
    val serviceMethod = service.bidiStreaming _

    "set tracing headers" in {
      (req ~> serviceMethod) {
        header[`X-Request-Id`]        shouldEqual Option(`x-request-id`)
        header[`X-B3-TraceId`]        shouldEqual Option(`x-b3-traceid`)
        header[`X-B3-SpanId`]         shouldEqual Option(`x-b3-spanid`)
        header[`X-B3-ParentSpanId`]   shouldEqual Option(`x-b3-parentspanid`)
        header[`X-Ot-Span-Context`]   shouldEqual Option(`x-ot-span-context`)
        header[`X-Client-Trace-Id`]   shouldEqual None
        header[`X-Envoy-Force-Trace`] shouldEqual None
        header[`X-B3-Sampled`]        shouldEqual None
        header[`X-B3-Flags`]          shouldEqual None
      }
    }

    "deliver the response to the client" in {
      (req ~> serviceMethod) {
        response shouldEqual req.map(r => Response(r.value + 1))
      }
    }
  }
}

object ServerSpec {
  trait DependencyPreinitializer { this: TestKit =>
    implicit val streamMaterializer = ActorMaterializer()(system)
  }
}
