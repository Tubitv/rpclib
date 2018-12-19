package com.tubitv.rpclib

import scala.collection.immutable.Seq

import protocbridge.JvmGenerator

package object compiler {

  /** TODO(dan): Scaladoc */
  final val RpclibRuntimeVersion = "2.0.0-SNAPSHOT"

  /** Entry point for Scala RPC wrapper compiler plugin.
   *
   *  TODO(dan): Scaladoc
   */
  def gen(
    flatPackage        : Boolean = false,
    javaConversions    : Boolean = false,
    grpc               : Boolean = true,
    singleLineToString : Boolean = false
  ): (JvmGenerator, Seq[String]) = {

    val generator = JvmGenerator("scala", RpcLibCodeGenerator)
    val params = Seq(
      "flat_package"          -> flatPackage,
      "java_conversions"      -> javaConversions,
      "grpc"                  -> grpc,
      "single_line_to_string" -> singleLineToString
    ).collect {
      case (name, v) if v => name
    }

    (generator, params)
  }
}
