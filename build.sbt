organization in ThisBuild := "com.example"

scalaVersion in ThisBuild := "2.12.4"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java


lazy val root = (project in file("."))
  .settings(name := "online-auction-java")
  .aggregate(
    tools, testkit, security,
    itemApi, itemImpl,
    biddingApi, biddingImpl,
    userApi, userImpl,
    transactionApi, transactionImpl,
    searchApi, searchImpl,
    webGateway)
  .settings(commonSettings: _*)

lazy val security = (project in file("security"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslServer % Optional
    )
  )


lazy val testkit = (project in file("testkit"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslPersistenceCassandra
    )
  )
  .dependsOn(tools)

lazy val itemApi = (project in file("item-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val itemImpl = (project in file("item-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomCdiServer,
      lagomCdiPersistence,
      lagomPersistenceReactiveMessaging,
      lagomReactiveMessaging,
      cassandraExtras
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(
    tools,
    testkit % "test",
    itemApi,
    biddingApi
  )

lazy val biddingApi = (project in file("bidding-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security)

lazy val biddingImpl = (project in file("bidding-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomJava)
  .dependsOn(biddingApi, itemApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomCdiServer,
      lagomCdiPersistence,
      lagomPersistenceReactiveMessaging,
      lagomReactiveMessaging
    ),
    maxErrors := 10000

  )

lazy val searchApi = (project in file("search-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val searchImpl = (project in file("search-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      lagomCdiServer,
      lagomReactiveMessaging,
      lombok
    ),
    testOptions in Test += Tests.Argument(TestFrameworks.JUnit, elasticsearch)
  )
  .dependsOn(tools, searchApi, itemApi, biddingApi)

lazy val tools = (project in file("tools"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )


lazy val transactionApi = (project in file("transaction-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, itemApi)

lazy val transactionImpl = (project in file("transaction-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomJava)
  .dependsOn(
    transactionApi,
    itemApi,
    tools,
    testkit % "test"
  ).settings(
  version := "1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    lagomJavadslPersistenceCassandra,
    lagomJavadslTestKit,
    lagomCdiServer,
    lagomCdiPersistence,
    lagomPersistenceReactiveMessaging,
    lagomReactiveMessaging,
    cassandraExtras
  )
)

lazy val userApi = (project in file("user-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val userImpl = (project in file("user-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomJava)
  .dependsOn(userApi, tools,
    testkit % "test"
  )
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      "de.svenkubiak" % "jBCrypt" % "0.4",
      lagomCdiServer,
      lagomCdiPersistence,
      lagomPersistenceReactiveMessaging,
      lagomReactiveMessaging,
      cassandraExtras,
      lombok
    )
  )

lazy val webGateway = (project in file("web-gateway"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, LagomPlay)
  .disablePlugins(PlayLayoutPlugin) // use the standard sbt layout... src/main/java, etc.
  .dependsOn(tools, transactionApi, biddingApi, itemApi, searchApi, userApi, searchApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient,
      lagomCdi,
      "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final",
      "org.webjars" % "foundation" % "6.2.3",
      "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"
    ),

    PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value,

    // Workaround for https://github.com/lagom/online-auction-java/issues/22
    // Uncomment the commented out line and remove the Scala line when issue #22 is fixed
    EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.ScalaIDE,
    // EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
    EclipseKeys.preTasks := Seq(compile in Compile)
  )

val lombok = "org.projectlombok" % "lombok" % "1.16.10"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0"
val lagomMicroprofileVersion = "1.0.0-alpha-jroper-1"
val lagomCdi = "com.lightbend.lagom.microprofile" %% "lagom-cdi" % lagomMicroprofileVersion
val lagomCdiServer = "com.lightbend.lagom.microprofile" %% "lagom-cdi-server" % lagomMicroprofileVersion
val lagomCdiPersistence = "com.lightbend.lagom.microprofile" %% "lagom-cdi-persistence" % lagomMicroprofileVersion
val lagomReactiveMessaging = "com.lightbend.lagom.microprofile" %% "lagom-reactive-messaging" % lagomMicroprofileVersion
val lagomPersistenceReactiveMessaging = "com.lightbend.lagom.microprofile" %% "lagom-persistence-messaging" % lagomMicroprofileVersion

def elasticsearch: String = {
  val enableElasticsearch = sys.props.getOrElse("enableElasticsearch", default = "false")
  if (enableElasticsearch == "true") {
    "--include-categories=com.example.auction.search.impl.ElasticsearchTests"
  } else {
    "--exclude-categories=com.example.auction.search.impl.ElasticsearchTests"
  }
}

def commonSettings: Seq[Setting[_]] = eclipseSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"),
  excludeDependencies += "com.typesafe.play" %% "play-guice"
)

lagomCassandraCleanOnStart in ThisBuild := false

// ------------------------------------------------------------------------------------------------

// register 'elastic-search' as an unmanaged service on the service locator so that at 'runAll' our code
// will resolve 'elastic-search' and use it. See also com.example.com.ElasticSearch
lagomUnmanagedServices in ThisBuild += ("elastic-search" -> "http://127.0.0.1:9200")

resolvers in ThisBuild ++= Seq(
  Resolver.bintrayRepo("jroper", "maven")
)
