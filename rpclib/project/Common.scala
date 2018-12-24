import sbt.Keys._
import sbt.librarymanagement.Resolver

object Common {

  private final val RpclibVersion = "2.0.0-SNAPSHOT"

  lazy val settings =
    Seq(
      version       := RpclibVersion,
      organization  := "com.tubitv.rpclib",
      scalaVersion  := "2.12.6",
      javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
      resolvers += Resolver.typesafeIvyRepo("releases")
    )
}
