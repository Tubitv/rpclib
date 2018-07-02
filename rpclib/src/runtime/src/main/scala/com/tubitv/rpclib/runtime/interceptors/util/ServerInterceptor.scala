package com.tubitv.rpclib.runtime.interceptors.util

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.{ ServerInterceptor => GrpcServerInterceptor }

/** Convenience class that makes implementing server interceptors easier. */
abstract class ServerInterceptor extends GrpcServerInterceptor {

  def onCall(requestHeaders: Metadata, responseHeaders: Metadata): Unit = ()

  final def interceptCall[Req, Resp](
    call: ServerCall[Req, Resp],
    requestHeaders: Metadata,
    next: ServerCallHandler[Req, Resp]
  ): ServerCall.Listener[Req] = {
    // When will Scala shuffle off this Javan coil?...
    next.startCall(
      new SimpleForwardingServerCallJavaInteropBridge[Req, Resp](call) {
        override def sendHeaders(responseHeaders: Metadata): Unit = {
          onCall(requestHeaders, responseHeaders)
          super.sendHeaders(responseHeaders)
        }
      },
      requestHeaders
    )
  }
}

object ServerInterceptor {

  /** Convenience function for creating an anonymous `ServerInterceptor`. */
  def onCall(f: (Metadata, Metadata) => Unit): ServerInterceptor = {
    new ServerInterceptor {
      override def onCall(requestHeaders: Metadata, responseHeaders: Metadata): Unit = {
        f(requestHeaders, responseHeaders)
      }
    }
  }
}
