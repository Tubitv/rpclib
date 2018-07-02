package com.tubitv

import scala.util.Random

package object rpclib {

  /** Returns the next pseudorandom, uniformly distributed int value between
   *  `min` (inclusive) and `max` (exclusive), drawn from the default Scala
   *  random number generator's sequence.
   *
   *  TODO(dan): Remove this once ScalaCheck is in place.
   */
  def nextInt(min: Int, max: Int): Int =
    min + Random.nextInt(max - min)
}
