package com.tubitv.rpclib.runtime

import akka.stream.scaladsl.Flow
import io.grpc.ServerCallHandler

object GrpcAkkaStreamsServerCalls {

  def unaryCall[Req, Resp, NotUsed](flow: Flow[Req, Resp, NotUsed]): ServerCallHandler[Req, Resp] = ???
  def serverStreamingCall[Req, Resp, NotUsed](flow: Flow[Req, Resp, NotUsed]): ServerCallHandler[Req, Resp] = ???
  def clientStreamingCall[Req, Resp, NotUsed](flow: Flow[Req, Resp, NotUsed]): ServerCallHandler[Req, Resp] = ???
  def bidiStreamingCall[Req, Resp, NotUsed](flow: Flow[Req, Resp, NotUsed]): ServerCallHandler[Req, Resp] = ???

}
