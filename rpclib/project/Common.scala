import sbt._
import sbt.Keys._

object Common {

  private final val RpclibVersion = "1.0.0"

  lazy val settings =
    Seq(
      version       := RpclibVersion,
      organization  := "io.github.tubitv",
      scalaVersion  := "2.12.6",
      homepage := Some(url("https://github.com/Tubitv/rpclib")),
      scmInfo := Some(ScmInfo(url("https://github.com/Tubitv/rpclib"),
                                  "git@github.com:Tubitv/rpclib.git")),
      developers := List(Developer("CatTail",
        "Chiyu Zhong",
        "chiyu@tubi.tv",
        url("https://github.com/CatTail"))),
      licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
      javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
      publishMavenStyle := true,
      publishTo := Some(
        if (isSnapshot.value)
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
      )
    )
}
