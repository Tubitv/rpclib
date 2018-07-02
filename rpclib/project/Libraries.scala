import sbt._

import scalapb.compiler.Version.grpcJavaVersion
import scalapb.compiler.Version.scalapbVersion

object Libraries {
  val akkaStreams     = "com.typesafe.akka"    %% "akka-stream"      % "2.5.13"
  val grpcStub        = "io.grpc"               % "grpc-stub"        % grpcJavaVersion
  val reactiveStreams = "org.reactivestreams"   % "reactive-streams" % "1.0.2"
  val scalapbCompiler = "com.thesamet.scalapb" %% "compilerplugin"   % scalapbVersion
}
