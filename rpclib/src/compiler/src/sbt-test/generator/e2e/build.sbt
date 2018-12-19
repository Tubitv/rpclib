import com.tubitv.rpclib.compiler.RpcLibCodeGenerator

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value,

  RpcLibCodeGenerator -> (sourceManaged in Compile).value
)
