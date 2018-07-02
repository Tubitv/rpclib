package com.tubitv.rpclib.testkit.spies

import com.tubitv.rpclib.runtime.FailurePolicy.CircuitBreakerPolicy
import com.tubitv.rpclib.runtime.FailurePolicy.DeadlinePolicy
import com.tubitv.rpclib.runtime.FailurePolicy.RetryPolicy
import com.tubitv.rpclib.testkit.TestEnvironment

/** Mixin to spy on failure policies.
 *
 *  Since ScalaMock 4.0 sadly doesn't provide spies, we must put together our
 *  own poor man's spy using a mutable variable that will be set to the value
 *  of the desired argument when the relevant method is called.
 */
private[testkit] trait FailurePolicySpies extends TestEnvironment {

  private[testkit] var circuitBreakerPolicySpy: CircuitBreakerPolicy = _
  private[testkit] var deadlinePolicySpy: DeadlinePolicy = _
  private[testkit] var retryPolicySpy: RetryPolicy = _

  /** Allows tests to read value of circuit breaker policy spy. */
  final def circuitBreakerPolicy: CircuitBreakerPolicy = circuitBreakerPolicySpy

  /** Allows tests to read value of deadline policy spy. */
  final def deadlinePolicy: DeadlinePolicy = deadlinePolicySpy

  /** Allows tests to read value of retry policy spy. */
  final def retryPolicy: RetryPolicy = retryPolicySpy
}
