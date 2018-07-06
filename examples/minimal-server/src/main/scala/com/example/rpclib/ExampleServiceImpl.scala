package com.example.rpclib

import scala.collection.immutable.Iterable

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.tubitv.rpclib.runtime.headers.GrpcHeader

class ExampleServiceImpl extends ExampleService.ExampleService {

  def unary(implicit headers: Iterable[GrpcHeader[_]]): Flow[Request, Response, NotUsed] =
    Flow[Request].map { case Request(id) =>
      Response(id)
    }

  def serverStreaming(implicit headers: Iterable[GrpcHeader[_]]): Flow[Request, Response, NotUsed] =
    Flow[Request].map { case Request(id) =>
      Response(id)
    }

  def clientStreaming(implicit headers: Iterable[GrpcHeader[_]]): Flow[Request, Response, NotUsed] =
    Flow[Request].map { case Request(id) =>
      Response(id)
    }

  def bidiStreaming(implicit headers: Iterable[GrpcHeader[_]]): Flow[Request, Response, NotUsed] =
    Flow[Request].map { case Request(id) =>
      Response(id)
    }
}
