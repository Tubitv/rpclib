package com.tubitv.rpclib.runtime

import scala.concurrent.duration._

import FailurePolicy._

final case class FailurePolicy(
  circuitBreaker: CircuitBreakerPolicy = CircuitBreakerPolicy.Default,
  deadline: DeadlinePolicy = DeadlinePolicy.Default,
  retries: RetryPolicy = RetryPolicy.Default
)

object FailurePolicy {

  final val Default: FailurePolicy = FailurePolicy()

  final case class CircuitBreakerPolicy(failures: Int, cooldown: FiniteDuration) {
    require(failures > 0, "CircuitBreakerPolicy: `failures` must be positive")
    require(cooldown > Duration.Zero, "CircuitBreakerPolicy: `cooldown` must be positive")
  }

  object CircuitBreakerPolicy {
    final val Default: CircuitBreakerPolicy =
      CircuitBreakerPolicy(failures = 3, cooldown = 5.seconds)
  }

  final case class DeadlinePolicy(duration: FiniteDuration) {
    require(duration > Duration.Zero, "DeadlinePolicy: `duration` must be positive")
  }

  object DeadlinePolicy {
    import scala.language.implicitConversions

    final val Default: DeadlinePolicy =
      DeadlinePolicy(duration = 5.seconds)

    implicit def fromFiniteDuration(duration: FiniteDuration): DeadlinePolicy =
      this.apply(duration)
  }

  final case class RetryPolicy(
    tries: Int,
    backoff: RetryPolicy.BackoffStrategy,
    jitter: RetryPolicy.Jitter = RetryPolicy.Jitter.None
  ) {
    require(tries > 0, "RetryPolicy: `tries` must be positive")
  }

  object RetryPolicy {
    final val Default: RetryPolicy =
      RetryPolicy(
        tries = 3,
        backoff = BackoffStrategy.Exponential(5.seconds),
        jitter = Jitter.Full
      )

    sealed trait BackoffStrategy
    object BackoffStrategy {
      final case class Constant(delay: FiniteDuration) extends BackoffStrategy {
        require(delay > Duration.Zero, "BackoffStrategy.Constant: `delay` must be positive")
      }

      final case class Exponential(halfLife: FiniteDuration) extends BackoffStrategy {
        require(halfLife > Duration.Zero, "BackoffStrategy.Exponential: `halfLife` must be positive")
      }
    }

    sealed trait Jitter
    object Jitter {
      case object None extends Jitter
      case object Equal extends Jitter
      case object Decorrelated extends Jitter
      case object Full extends Jitter
    }
  }

  /** Alias that clarifies intent of `null` usage when constructing `FailurePolicy` instances.
   *
   *  For instance, the intent is clear in the following case that the deadline policy is to fall
   *  through to the next highest priority policy:
   *
   *  {{{
   *      val policy = FailurePolicy(deadline = Fallthrough)
   *  }}}
   *
   *  Contrast that to the following code, which is functionally identical but less clear:
   *
   *  {{{
   *      val policy = FailurePolicy(deadline = null)
   *  }}}
   */
  final val Fallthrough = null
}
