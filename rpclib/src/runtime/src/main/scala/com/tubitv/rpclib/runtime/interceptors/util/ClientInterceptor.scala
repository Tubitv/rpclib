package com.tubitv.rpclib.runtime.interceptors.util

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.{ ClientInterceptor => GrpcClientInterceptor }

/** Convenience class that makes implementing client interceptors easier. */
abstract class ClientInterceptor extends GrpcClientInterceptor {

  def onRequest(requestHeaders: Metadata, callOptions: CallOptions): Unit = ()
  def onResponse(responseHeaders: Metadata, callOptions: CallOptions): Unit = ()

  final def interceptCall[Req, Resp](
    method: MethodDescriptor[Req, Resp],
    callOptions: CallOptions,
    next: Channel
  ): ClientCall[Req, Resp] = {
    // Oh Java, how I loathe you...
    new SimpleForwardingClientCall[Req, Resp](next.newCall(method, callOptions)) {
      override def start(
        responseListener: ClientCall.Listener[Resp],
        requestHeaders: Metadata
      ): Unit = {
        onRequest(requestHeaders, callOptions)
        super.start(
          new SimpleForwardingClientCallListenerJavaInteropBridge[Resp](responseListener) {
            override def onHeaders(responseHeaders: Metadata): Unit = {
              onResponse(responseHeaders, callOptions)
              super.onHeaders(responseHeaders)
            }
          },
          requestHeaders
        )
      }
    }
  }
}

object ClientInterceptor {

  /** Convenience function for creating an anonymous `ClientInterceptor`. */
  def onRequest(f: Metadata => Unit): ClientInterceptor = {
    new ClientInterceptor {
      override def onRequest(requestHeaders: Metadata, callOptions: CallOptions): Unit = {
        f(requestHeaders)
      }
    }
  }

  /** Convenience function for creating an anonymous `ClientInterceptor`. */
  def onRequest(f: (Metadata, CallOptions) => Unit): ClientInterceptor = {
    new ClientInterceptor {
      override def onRequest(requestHeaders: Metadata, callOptions: CallOptions): Unit = {
        f(requestHeaders, callOptions)
      }
    }
  }

  /** Convenience function for creating an anonymous `ClientInterceptor`. */
  def onResponse(f: Metadata => Unit): ClientInterceptor = {
    new ClientInterceptor {
      override def onResponse(responseHeaders: Metadata, callOptions: CallOptions): Unit = {
        f(responseHeaders)
      }
    }
  }

  /** Convenience function for creating an anonymous `ClientInterceptor`. */
  def onResponse(f: (Metadata, CallOptions) => Unit): ClientInterceptor = {
    new ClientInterceptor {
      override def onResponse(responseHeaders: Metadata, callOptions: CallOptions): Unit = {
        f(responseHeaders, callOptions)
      }
    }
  }
}
