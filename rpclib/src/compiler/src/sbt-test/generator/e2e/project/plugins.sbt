val pluginVersion = sys.props.get("plugin.version").getOrElse(
        sys.error("""|The system property 'plugin.version' is not defined.
                     |Specify this property by passing a version to SBT, for
                     |example -Dplugin.version=0.1.0-SNAPSHOT""".stripMargin
                 )
)

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.19")

libraryDependencies += "com.tubitv.rpclib" %% "rpclib-compiler" % pluginVersion
