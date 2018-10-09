import sbt._

import com.trueaccord.scalapb.compiler.Version.{scalapbVersion, grpcJavaVersion}

object Libraries {
  val akkaStreams        = "com.typesafe.akka"      %% "akka-stream"          % "2.5.13"
  val grpcStub           = "io.grpc"                % "grpc-stub"             % grpcJavaVersion
  val reactiveStreams    = "org.reactivestreams"    % "reactive-streams"      % "1.0.2"
  val scalapbCompiler    = "com.trueaccord.scalapb" %% "compilerplugin"       % scalapbVersion
  val scalapbGrpcRuntime = "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion
}
