package com.tubitv.rpclib.testkit.spies

import io.grpc.Metadata

import com.tubitv.rpclib.runtime.headers.GrpcHeader
import com.tubitv.rpclib.runtime.headers.GrpcHeaderCompanion
import com.tubitv.rpclib.testkit.TestEnvironment

/** Mixin to spy on gRPC request and response headers.
 *
 *  Since ScalaMock 4.0 sadly doesn't provide spies, we must put together our
 *  own poor man's spy using a mutable variable that will be set to the value
 *  of the desired argument when the relevant method is called.
 */
private[testkit] trait MetadataSpy extends TestEnvironment {

  private[testkit] var metadataSpy: Metadata = new Metadata

  /** Allows tests to read value of spy. */
  final def header[Header <: GrpcHeader[_]](implicit companion: GrpcHeaderCompanion[Header]): Option[String] =
    Option(metadataSpy.get(companion.key))
}
