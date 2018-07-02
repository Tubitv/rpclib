package com.tubitv.rpclib.testkit

import scala.collection.immutable.Seq

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import com.tubitv.rpc.Request
import com.tubitv.rpc.Response
import com.tubitv.rpc.TestService
import com.tubitv.rpc.TestServiceGrpc
import com.tubitv.rpclib.runtime.headers.EnvoyHeaders
import com.tubitv.rpclib.runtime.interceptors.EnvoyHeadersClientInterceptor
import com.tubitv.rpclib.runtime.interceptors.util.ClientInterceptor
import com.tubitv.rpclib.testkit.dsl.ServerTestDSL
import com.tubitv.rpclib.testkit.spies.GrpcHeadersSpy
import com.tubitv.rpclib.testkit.spies.ResponseSpy

trait ServerTestEnvironment extends TestEnvironment
                               with ServerTestDSL
                               with GrpcHeadersSpy
                               with ResponseSpy {

  implicit val streamMaterializer: Materializer

  serviceRegistry.addService(
    TestService.bindService(
      new TestService.TestService {
        /** Adds one to the element. */
        def unary(implicit headers: EnvoyHeaders): Flow[Request, Response, NotUsed] = {
          grpcHeadersSpy = headers
          Flow[Request].map(request => Response(request.value + 1))
        }

        /** Returns as many elements as the first value sent by the client. */
        def serverStreaming(implicit headers: EnvoyHeaders): Flow[Request, Response, NotUsed] = {
          grpcHeadersSpy = headers
          Flow[Request].mapConcat { case Request(n) =>
            Seq.iterate(1, n)(_ + 1).map(Response.apply)
          }
        }

        /** Returns the sum of the elements sent by the client. */
        def clientStreaming(implicit headers: EnvoyHeaders): Flow[Request, Response, NotUsed] = {
          grpcHeadersSpy = headers
          Flow[Request].fold(Response(0)) {
            case (Response(r), Request(s)) => Response(r + s)
          }
        }

        /** Adds one to each element. */
        def bidiStreaming(implicit headers: EnvoyHeaders): Flow[Request, Response, NotUsed] = {
          grpcHeadersSpy = headers
          Flow[Request].map(request => Response(request.value + 1))
        }
      }
    )
  )

  val clientStub =
    TestServiceGrpc.stub(channel)
      .withInterceptors(new EnvoyHeadersClientInterceptor)
}
