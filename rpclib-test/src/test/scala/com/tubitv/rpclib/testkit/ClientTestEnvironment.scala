package com.tubitv.rpclib.testkit

import scala.collection.immutable.Seq
import scala.concurrent.Future

import io.grpc.ServerInterceptors
import io.grpc.stub.StreamObserver

import com.tubitv.rpc.Request
import com.tubitv.rpc.Response
import com.tubitv.rpc.TestServiceGrpc
import com.tubitv.rpclib.runtime.interceptors.util.ServerInterceptor
import com.tubitv.rpclib.testkit.dsl.ClientTestDSL
import com.tubitv.rpclib.testkit.spies.FailurePolicySpies
import com.tubitv.rpclib.testkit.spies.MetadataSpy
import com.tubitv.rpclib.testkit.spies.RequestSpy

trait ClientTestEnvironment extends TestEnvironment
                               with ClientTestDSL
                               with MetadataSpy
                               with RequestSpy
                               with FailurePolicySpies {

  serviceRegistry.addService(
    ServerInterceptors.intercept(
      TestServiceGrpc.bindService(
        new TestServiceGrpc.TestService {
          def unary(request: Request): Future[Response] = {
            requestSpy = Seq(request)
            Future.successful(Response())
          }

          def serverStreaming(request: Request, responseObserver: StreamObserver[Response]): Unit = {
            requestSpy = Seq(request)
            responseObserver.onNext(Response())
            responseObserver.onCompleted()
          }

          def clientStreaming(responseObserver: StreamObserver[Response]): StreamObserver[Request] =
            fullStreaming(responseObserver)

          def bidiStreaming(responseObserver: StreamObserver[Response]): StreamObserver[Request] =
            fullStreaming(responseObserver)

          private def fullStreaming(responseObserver: StreamObserver[Response]): StreamObserver[Request] = {
            requestSpy = Seq.empty[Request]
            new StreamObserver[Request] {
              def onNext(value: Request): Unit = {
                // Inefficient, but performance penalty is negligible in these tests
                requestSpy = requestSpy :+ value
              }
              def onCompleted(): Unit = {
                responseObserver.onNext(Response())
                responseObserver.onCompleted()
              }
              def onError(t: Throwable): Unit = ()
            }
          }
        },
        scala.concurrent.ExecutionContext.global
      ),
      ServerInterceptor.onCall((metadata, _) => metadataSpy = metadata)
    )
  )
}
