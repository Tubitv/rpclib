package com.tubitv.rpclib.runtime.interceptors

import scala.collection.immutable.Iterable

import io.grpc.CallOptions
import io.grpc.Metadata

import com.tubitv.rpclib.runtime.headers.GrpcHeader
import com.tubitv.rpclib.runtime.interceptors.util.ClientInterceptor

class EnvoyHeadersClientInterceptor extends ClientInterceptor {
  import EnvoyHeadersClientInterceptor._
  override def onRequest(requestHeaders: Metadata, callOptions: CallOptions): Unit = {
    for (h <- callOptions.getOption(HeadersKey))
      requestHeaders.put(h.key, h.value.toString)
  }
}

object EnvoyHeadersClientInterceptor {
  private[this] final val KeyName = "envoy-headers"
  final val HeadersKey = CallOptions.Key.of(KeyName, Iterable.empty[GrpcHeader[_]])
}
