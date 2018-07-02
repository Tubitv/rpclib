package com.tubitv.rpclib.testkit

import java.util.UUID

import io.grpc.HandlerRegistry
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.ServerServiceDefinition
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.util.MutableHandlerRegistry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite

/** Test fixtures common to the client and the server test environments.
 *
 *  The behavior of these fixtures are translated from the official
 *  `GrpcServerRule` implementation found at:
 *
 *  https://github.com/grpc/grpc-java/blob/v1.9.0/testing/src/main/java/io/grpc/testing/GrpcServerRule.java
 */
private[testkit] trait TestEnvironment extends Suite with BeforeAndAfterAll {

  private val serverName: String = UUID.randomUUID().toString

  private[testkit] val serviceRegistry =
    new MutableHandlerRegistry

  private val server: Server =
    InProcessServerBuilder
      .forName(serverName)
      .fallbackHandlerRegistry(serviceRegistry)
      .build()
      .start()

  val channel: ManagedChannel =
    InProcessChannelBuilder
      .forName(serverName)
      .build()

  override def afterAll(): Unit = {
    channel.shutdown()
    server.shutdown()
  }
}
