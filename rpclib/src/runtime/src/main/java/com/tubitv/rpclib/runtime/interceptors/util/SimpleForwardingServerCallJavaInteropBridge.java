package com.tubitv.rpclib.runtime.interceptors.util;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;

/**
 * This class exists solely as a workaround for Scala compiler bug SI-7936.
 *
 * The Scala compiler has a Java interoperability bug in which a child class written in Scala cannot
 * call the methods of a grandparent class written in Java.
 *
 * The workaround below defines an intermediate parent class in Java, from which the Scala class
 * will directly inherit, that calls the grandparent method.
 *
 * @see <a href="https://issues.scala-lang.org/browse/SI-7936">SI-7936</a>
 */
abstract class SimpleForwardingServerCallJavaInteropBridge<ReqT, RespT>
    extends SimpleForwardingServerCall<ReqT, RespT> {

  protected SimpleForwardingServerCallJavaInteropBridge(ServerCall<ReqT, RespT> delegate) {
    super(delegate);
  }

  @Override
  public void sendHeaders(Metadata headers) {
    super.sendHeaders(headers);
  }
}
