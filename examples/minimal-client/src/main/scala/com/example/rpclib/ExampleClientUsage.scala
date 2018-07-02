package com.example.rpclib

import scala.collection.immutable.Seq
import scala.concurrent.Future

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.tubitv.rpclib.runtime.headers.EnvoyHeaders

class ExampleClientUsage(client: ExampleService.ExampleService)(implicit materializer: Materializer) {

  def unary(request: Request)(implicit hdrs: EnvoyHeaders): Future[Response] = {
    Source.single(request)
      .via(client.unary)
      .runWith(Sink.head)
  }

  def serverStreaming(request: Request)(implicit hdrs: EnvoyHeaders): Future[Seq[Response]] = {
    Source.single(request)
      .via(client.serverStreaming)
      .runWith(Sink.seq)
  }

  def clientStreaming(request: Seq[Request])(implicit hdrs: EnvoyHeaders): Future[Response] = {
    Source(request)
      .via(client.clientStreaming)
      .runWith(Sink.head)
  }

  def bidiStreaming(request: Seq[Request])(implicit hdrs: EnvoyHeaders): Future[Seq[Response]] = {
    Source(request)
      .via(client.bidiStreaming)
      .runWith(Sink.seq)
  }
}
