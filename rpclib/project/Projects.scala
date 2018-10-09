import sbt._
import sbt.Keys._

object Projects {

  // The latest version of each release as of June 26, 2018.
  private val `scala-2.10` = "2.10.7"
  private val `scala-2.11` = "2.11.12"
  private val `scala-2.12` = "2.12.6"
  private val `sbt-0.13`   = "0.13.17"
  private val `sbt-1.x`    = "1.1.6"

  lazy val root =
    (project in file("."))
      .settings(
        name := "rpclib",

        Common.settings,

        // Prevent the `publish*` commands from creating artifacts for
        // the root project.
        publish      := {},
        publishLocal := {},
        publishM2    := {},
        skip in publish := true
      )
      .aggregate(compiler, runtime)

  lazy val compiler =
    Project(id = "rpclib-compiler", base = file("src/compiler"))
      .settings(
        sbtPlugin := true,

        Common.settings,

        // This fine specimen of an ugly hack is brought to you by SBT
        // bug #3473, wherein cross-building for an SBT plugin breaks if
        // the plugin is a subproject.  See the following for details:
        // `https://github.com/sbt/sbt/issues/3473`.
        crossScalaVersions := Seq(`scala-2.10`, `scala-2.12`),
        sbtVersion in pluginCrossBuild := {
          scalaBinaryVersion.value match {
            case "2.10" => `sbt-0.13`
            case "2.12" => `sbt-1.x`
          }
        },

        libraryDependencies += Libraries.scalapbCompiler
      )

  lazy val runtime =
    Project(id = "rpclib-runtime", base = file("src/runtime"))
      .settings(
        Common.settings,

        crossScalaVersions := Seq(`scala-2.11`, `scala-2.12`),

        libraryDependencies ++= Seq(
          Libraries.akkaStreams,
          Libraries.grpcStub,
          Libraries.reactiveStreams,
          Libraries.scalapbGrpcRuntime
        )
      )
}
