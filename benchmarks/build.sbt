import com.tubitv.rpclib.compiler.RpcLibCodeGenerator

lazy val root = project.in(file("."))
  .dependsOn(
    ProjectRef(file("../rpclib/"), "rpclib-compiler"),
    ProjectRef(file("../rpclib/"), "rpclib-runtime")
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-testing" % scalapb.compiler.Version.grpcJavaVersion,
      "org.hdrhistogram" % "HdrHistogram" % "2.1.10",
      "org.apache.commons" % "commons-math3" % "3.6",
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "io.grpc" % "grpc-stub" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    )
  )
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value,
      RpcLibCodeGenerator -> (sourceManaged in Compile).value
    )
  )
