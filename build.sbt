name := "auth"

version := "1.0"

lazy val `auth` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  "org.keycloak"          % "keycloak-admin-client" % "18.0.0",
  "org.keycloak"          % "keycloak-core"         % "18.0.0",
  "org.keycloak"          % "keycloak-adapter-core" % "18.0.0",
  "org.passay"            % "passay"                % "1.6.1",
  "com.github.jwt-scala" %% "jwt-circe"             % "9.0.2",
  // Circe
  "io.circe" %% "circe-core"           % "0.14.1",
  "io.circe" %% "circe-generic"        % "0.14.1",
  "io.circe" %% "circe-generic-extras" % "0.14.1",
  "io.circe" %% "circe-parser"         % "0.14.1",
  //Cats
  "org.typelevel" %% "cats-core"   % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.4",
  specs2           % Test,
  guice
)

dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core"        % "2.11.4",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.11.4",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.11.4",
)

Universal / javaOptions ++= Seq("-Dpidfile.path=/dev/null")
