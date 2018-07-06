package com.tubitv.rpclib.testkit.spies

import scala.collection.immutable.Iterable
import scala.reflect.ClassTag
import scala.reflect.classTag

import com.tubitv.rpclib.runtime.headers.GrpcHeader
import com.tubitv.rpclib.runtime.headers.GrpcHeaderCompanion
import com.tubitv.rpclib.testkit.TestEnvironment

/** Mixin to spy on gRPC request and response headers.
 *
 *  Since ScalaMock 4.0 sadly doesn't provide spies, we must put together our
 *  own poor man's spy using a mutable variable that will be set to the value
 *  of the desired argument when the relevant method is called.
 */
private[testkit] trait GrpcHeadersSpy extends TestEnvironment {

  private[testkit] var grpcHeadersSpy: Iterable[GrpcHeader[_]] = Iterable.empty

  /** Allows tests to read value of spy. */
  final def header[Header <: GrpcHeader[_] : ClassTag]: Option[Header] =
    grpcHeadersSpy.flatMap(classTag[Header].unapply).headOption
}
