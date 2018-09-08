package com.tubitv.rpclib.runtime

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import io.grpc.ClientCall
import io.grpc.stub.ClientCalls
import scalapb.grpc.Grpc

object GrpcAkkaStreamsClientCalls {

  def unaryFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] = {
    Flow[Req].flatMapConcat(req => {
      Source.fromFuture(Grpc.guavaFuture2ScalaFuture(ClientCalls.futureUnaryCall(call, req)))
    })
  }

  def serverStreamingFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] = ???

  def clientStreamingFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] = ???

  def bidiStreamingFlow[Req, Resp](call: ClientCall[Req, Resp]): Flow[Req, Resp, NotUsed] = ???
}
