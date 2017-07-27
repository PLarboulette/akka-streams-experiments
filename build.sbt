name := "akka_streams_experiments"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies++=Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test,
  "com.typesafe.play" % "play-json_2.12" % "2.6.2"
)

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "sonatype-public" at "https://oss.sonatype.org/content/groups/public"
)
