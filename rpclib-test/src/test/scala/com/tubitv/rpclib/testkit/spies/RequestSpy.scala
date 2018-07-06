package com.tubitv.rpclib.testkit.spies

import com.tubitv.rpc.Request
import com.tubitv.rpclib.testkit.TestEnvironment

/** Mixin to spy on gRPC client requests.
 *
 *  Since ScalaMock 4.0 sadly doesn't provide spies, we must put together our
 *  own poor man's spy using a mutable variable that will be set to the value
 *  of the desired argument when the relevant method is called.
 */
private[testkit] trait RequestSpy extends TestEnvironment {

  private[testkit] var requestSpy: Seq[Request] = Seq.empty[Request]

  /** Allows tests to read value of spy. */
  final def request: Seq[Request] = requestSpy
}
