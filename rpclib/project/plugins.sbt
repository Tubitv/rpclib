addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.18")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.2"
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
