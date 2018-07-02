import com.tubitv.rpclib.compiler.RpcLibCodeGenerator

lazy val rpclibTest = (project in file("."))
  .settings(
    name         := "rpclib-test",
    organization := "com.tubitv",
    version      := "0.3.0",
    scalaVersion := "2.12.6",

    PB.protoSources in Compile := Seq(
      baseDirectory.value / "src/test/proto"
    ),

    PB.targets in Compile := Seq(
      scalapb.gen()       -> (sourceManaged in Compile).value,
      RpcLibCodeGenerator -> (sourceManaged in Compile).value
    ),

    libraryDependencies ++= Seq(
      "io.grpc"               % "grpc-netty"           % "1.13.1",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.7.4",
      "com.typesafe.akka"    %% "akka-testkit"         % "2.5.13" % Test,
      "org.scalamock"        %% "scalamock"            % "4.1.0"  % Test,
      "org.scalatest"        %% "scalatest"            % "3.0.5"  % Test
    )
  )
