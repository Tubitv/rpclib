package com.tubitv.rpclib.runtime.interceptors

import scala.collection.immutable.Iterable

import io.grpc.Context
import io.grpc.Contexts
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor

import com.tubitv.rpclib.runtime.headers.GrpcHeader

/** Server interceptor that extracts Envoy tracing headers from gRPC metadata. */
class EnvoyHeadersServerInterceptor extends ServerInterceptor {

  final def interceptCall[Req, Resp](
    call: ServerCall[Req, Resp],
    requestHeaders: Metadata,
    next: ServerCallHandler[Req, Resp]
  ): ServerCall.Listener[Req] = {
    // A `Context` is gRPC's way of setting the environment within which a
    // server-side method runs.  A context is comprised of key-value pairs.
    //
    // Below, we create a new `Context` by adding the Envoy tracing headers to a
    // predefined key and use a context interceptor to run the method.  In the
    // generated code, the headers are extracted from the context and passed to
    // the method being called.
    //
    // Official documentation for `Context` is a bit scarce as of this writing,
    // but `https://stackoverflow.com/a/43483510/5793773` gives a bit of insight
    // if you're curious how contexts work.

    val ctx = Context.current.withValue(
      EnvoyHeadersServerInterceptor.ContextKey,
      GrpcHeader.extractAll(requestHeaders)
    )

    Contexts.interceptCall(ctx, call, requestHeaders, next)
  }
}

object EnvoyHeadersServerInterceptor {
  private[this] final val KeyName = "envoy-headers"
  final val ContextKey = Context.key[Iterable[GrpcHeader[_]]](KeyName)
}
