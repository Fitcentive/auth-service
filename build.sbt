name := "auth"

version := "1.0"

lazy val `auth` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  "org.keycloak" % "keycloak-admin-client" % "18.0.0",
  "org.passay"   % "passay"                % "1.6.1",
  specs2         % Test,
  guice
)

Universal / javaOptions ++= Seq("-Dpidfile.path=/dev/null")
