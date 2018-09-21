import sbt.Keys._

object Common {

  private final val RpclibVersion = "1.0.0"

  lazy val settings =
    Seq(
      version       := RpclibVersion,
      organization  := "com.tubitv.rpclib",
      scalaVersion  := "2.12.6",
      javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
    )
}
