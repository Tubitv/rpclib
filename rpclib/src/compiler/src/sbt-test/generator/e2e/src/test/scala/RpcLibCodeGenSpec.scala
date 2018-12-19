package rpclib

import org.scalatest._
import io.tubi.rpclib.example._
import scalapb.GeneratedMessage

class RpcLibCodeGenSpec extends FlatSpec with MustMatchers {
  "Proto message case classes" should "be an instance of GeneratedMessage" in {
    Request().isInstanceOf[GeneratedMessage] must be(true)
    Response().isInstanceOf[GeneratedMessage] must be(true)
  }
}
