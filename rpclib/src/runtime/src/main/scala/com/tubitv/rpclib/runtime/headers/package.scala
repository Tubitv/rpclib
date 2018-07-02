package com.tubitv.rpclib.runtime

import scala.collection.immutable.Iterable

import io.grpc.Metadata

package object headers {

  // Just an alias to save users a few characters in their method signatures.
  type EnvoyHeaders = Iterable[GrpcHeader[_]]

  // These headers are used by Envoy for tracing.  For more information, see
  // `https://www.envoyproxy.io/docs/envoy/v1.5.0/intro/arch_overview/tracing.html`.

  /** Base class for the gRPC headers below. */
  sealed abstract class GrpcHeader[V] {
    protected type This <: GrpcHeader[V]
    protected def companion: GrpcHeaderCompanion[This]
    val key: Metadata.Key[String] = companion.key
    val value: V
  }

  object GrpcHeader {

    private[this] final val MetadataKeys = Iterable(
      `X-Request-Id`.key,
      `X-B3-TraceId`.key,
      `X-B3-SpanId`.key,
      `X-B3-ParentSpanId`.key,
      `X-B3-Sampled`.key,
      `X-B3-Flags`.key,
      `X-Ot-Span-Context`.key,
      `X-Client-Trace-Id`.key,
      `X-Envoy-Force-Trace`.key
    )

    def extractAll(metadata: Metadata): EnvoyHeaders = {
      MetadataKeys
        .filter(metadata.containsKey)
        .map {
          case k @ `X-Request-Id`.key        => `X-Request-Id`(metadata.get(k))
          case k @ `X-B3-TraceId`.key        => `X-B3-TraceId`(metadata.get(k))
          case k @ `X-B3-SpanId`.key         => `X-B3-SpanId`(metadata.get(k))
          case k @ `X-B3-ParentSpanId`.key   => `X-B3-ParentSpanId`(metadata.get(k))
          case k @ `X-B3-Sampled`.key        => `X-B3-Sampled`(metadata.get(k))
          case k @ `X-B3-Flags`.key          => `X-B3-Flags`(metadata.get(k))
          case k @ `X-Ot-Span-Context`.key   => `X-Ot-Span-Context`(metadata.get(k))
          case k @ `X-Client-Trace-Id`.key   => `X-Client-Trace-Id`(metadata.get(k))
          case k @ `X-Envoy-Force-Trace`.key => `X-Envoy-Force-Trace`(metadata.get(k))
        }
    }
  }

  /** Base class for the gRPC headers' companion objects. */
  sealed abstract class GrpcHeaderCompanion[_ <: GrpcHeader[_]] {
    val key: Metadata.Key[String]
  }

  /** `x-request-id` header
   *
   *  The `x-request-id` header is used by Envoy to uniquely identify a request as
   *  well as perform stable access logging and tracing.  Envoy will generate an
   *  `x-request-id` header for all external origin requests.  It will also
   *  generate an `x-request-id` header for internal requests that do not already
   *  have one.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#config-http-conn-man-headers-x-request-id
   */
  final case class `X-Request-Id`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-Request-Id`
    protected def companion = `X-Request-Id`
  }

  implicit object `X-Request-Id` extends GrpcHeaderCompanion[`X-Request-Id`] {
    val key = Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-b3-traceid` header
   *
   *  The `x-b3-traceid` HTTP header is used by the Zipkin tracer in Envoy.  The
   *  `TraceId` is 64 bits in length and indicates the overall ID of the trace.
   *  Every span in a trace shares this ID.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-b3-traceid
   */
  final case class `X-B3-TraceId`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-B3-TraceId`
    protected def companion = `X-B3-TraceId`
  }

  implicit object `X-B3-TraceId` extends GrpcHeaderCompanion[`X-B3-TraceId`] {
    val key = Metadata.Key.of("x-b3-traceid", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-b3-spanid` header
   *
   *  The `x-b3-spanid` HTTP header is used by the Zipkin tracer in Envoy.  The
   *  `SpanId` is 64 bits in length and indicates the position of the current
   *  operation in the trace tree.  The value should not be interpreted: it may or
   *  may not be derived from the value of the `TraceId`.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-b3-spanid
   */
  final case class `X-B3-SpanId`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-B3-SpanId`
    protected def companion = `X-B3-SpanId`
  }

  implicit object `X-B3-SpanId` extends GrpcHeaderCompanion[`X-B3-SpanId`] {
    val key = Metadata.Key.of("x-b3-spanid", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-b3-parentspanid` header
   *
   *  The `x-b3-parentspanid` HTTP header is used by the Zipkin tracer in Envoy.
   *  The `ParentSpanId` is 64 bits in length and indicates the position of the
   *  parent operation in the trace tree.  When the span is the root of the trace
   *  tree, the `ParentSpanId` is absent.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-b3-parentspanid
   */
  final case class `X-B3-ParentSpanId`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-B3-ParentSpanId`
    protected def companion = `X-B3-ParentSpanId`
  }

  implicit object `X-B3-ParentSpanId` extends GrpcHeaderCompanion[`X-B3-ParentSpanId`] {
    val key = Metadata.Key.of("x-b3-parentspanid", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-b3-sampled` header
   *
   *  The `x-b3-sampled` HTTP header is used by the Zipkin tracer in Envoy.  When
   *  the `Sampled` flag is `true`, the span will be reported to the tracing system.
   *  Once `Sampled` is set, the same value should be consistently sent
   *  downstream.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-b3-sampled
   */
  final case class `X-B3-Sampled`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-B3-Sampled`
    protected def companion = `X-B3-Sampled`
  }

  implicit object `X-B3-Sampled` extends GrpcHeaderCompanion[`X-B3-Sampled`] {
    val key = Metadata.Key.of("x-b3-sampled", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-b3-flags`
   *
   *  The `x-b3-flags` HTTP header is used by the Zipkin tracer in Envoy.  When
   *  the `Flags` flag is `true`, Zipkin will interpret the trace as a debug
   *  trace.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-b3-flags
   */
  final case class `X-B3-Flags`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-B3-Flags`
    protected def companion = `X-B3-Flags`
  }

  implicit object `X-B3-Flags` extends GrpcHeaderCompanion[`X-B3-Flags`] {
    val key = Metadata.Key.of("x-b3-flags", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-ot-span-context`
   *
   *  The `x-ot-span-context` HTTP header is used by Envoy to establish proper
   *  parent-child relationships between tracing spans.  This header can be used
   *  with both LightStep and Zipkin tracers.  For example, an egress span is a
   *  child of an ingress span (if the ingress span was present).  Envoy injects
   *  the `x-ot-span-context` header on ingress requests and forwards it to the
   *  local service.  Envoy relies on the application to propagate this header on
   *  the egress call to an upstream.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-ot-span-context
   */
  final case class `X-Ot-Span-Context`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-Ot-Span-Context`
    protected def companion = `X-Ot-Span-Context`
  }

  implicit object `X-Ot-Span-Context` extends GrpcHeaderCompanion[`X-Ot-Span-Context`] {
    val key = Metadata.Key.of("x-ot-span-context", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-client-trace-id` header
   *
   *  If an external client sets this header, Envoy will join the provided trace
   *  ID with the internally generated `x-request-id`.  `x-client-trace-id` needs
   *  to be globally unique.  Generating a UUID4 is recommended.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#x-client-trace-id
   */
  final case class `X-Client-Trace-Id`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-Client-Trace-Id`
    protected def companion = `X-Client-Trace-Id`
  }

  implicit object `X-Client-Trace-Id` extends GrpcHeaderCompanion[`X-Client-Trace-Id`] {
    val key = Metadata.Key.of("x-client-trace-id", Metadata.ASCII_STRING_MARSHALLER)
  }

  /** `x-envoy-force-trace` header
   *
   *  If an internal request sets this header, Envoy will modify the generated
   *  `x-request-id` such that it forces traces to be collected.  This also forces
   *  `x-request-id` to be returned in the response headers.  If this request ID
   *  is then propagated to other hosts, traces will also be collected on those
   *  hosts which will provide a consistent trace for an entire request flow.
   *
   *  Documentation: https://www.envoyproxy.io/docs/envoy/v1.5.0/configuration/http_conn_man/headers.html#config-http-conn-man-headers-x-envoy-force-trace
   */
  final case class `X-Envoy-Force-Trace`(override val value: String) extends GrpcHeader[String] {
    protected type This = `X-Envoy-Force-Trace`
    protected def companion = `X-Envoy-Force-Trace`
  }

  implicit object `X-Envoy-Force-Trace` extends GrpcHeaderCompanion[`X-Envoy-Force-Trace`] {
    val key = Metadata.Key.of("x-envoy-force-trace", Metadata.ASCII_STRING_MARSHALLER)
  }
}
