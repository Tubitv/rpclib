import com.tubitv.rpclib.compiler.RpcLibCodeGenerator

lazy val root = (project in file("."))
  .settings(
    name         := "minimal-rpclib-client",
    version      := "0.1.0-SNAPSHOT",
    organization := "com.example",
    scalaVersion := "2.12.6",

    PB.protoSources in Compile := Seq(
      baseDirectory.value / "src/main/proto"
    ),

    PB.targets in Compile := Seq(
      scalapb.gen()       -> (sourceManaged in Compile).value,
      RpcLibCodeGenerator -> (sourceManaged in Compile).value
    )
  )
