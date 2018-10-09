import sbt._
import sbt.Keys._
import bintray.BintrayKeys._

object Common {

  private final val RpclibVersion = "1.0.0"

  lazy val settings =
    Seq(
      version       := RpclibVersion,
      organization  := "com.tubitv.rpclib",
      scalaVersion  := "2.12.6",
      javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
      bintrayReleaseOnPublish in ThisBuild := false,
      homepage := Some(url("https://github.com/Tubitv/rpclib")),
      scmInfo := Some(ScmInfo(url("https://github.com/Tubitv/rpclib"), "git@github.com:Tubitv/rpclib.git")),
      developers := List(Developer("CatTail", "Chiyu Zhong", "chiyu@tubi.tv", url("https://github.com/CatTail"))),
      licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
    )
}
