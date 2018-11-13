name := "forex"
version := "1.0.0"

scalaVersion := "2.12.4"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification",
  "-language:experimental.macros",
  "-language:implicitConversions"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.github.pureconfig"      %% "pureconfig"             % "0.7.2",
  "com.softwaremill.quicklens" %% "quicklens"              % "1.4.11",
  "com.typesafe.akka"          %% "akka-actor"             % "2.5.18",
  "com.typesafe.akka"          %% "akka-http"              % "10.1.5",
  "de.heikoseeberger"          %% "akka-http-circe"        % "1.22.0",
  "io.circe"                   %% "circe-core"             % "0.10.1",
  "io.circe"                   %% "circe-generic"          % "0.10.1",
  "io.circe"                   %% "circe-generic-extras"   % "0.10.1",
  "io.circe"                   %% "circe-java8"            % "0.10.1",
  "io.circe"                   %% "circe-jawn"             % "0.10.1",
  "org.atnos"                  %% "eff"                    % "5.1.0",
  "org.atnos"                  %% "eff-monix"              % "5.1.0",
  "com.github.cb372"           %% "scalacache-cats-effect" % "0.26.0",
  "com.github.cb372"           %% "scalacache-monix"       % "0.26.0",
  "com.github.cb372"           %% "scalacache-redis"       % "0.26.0",
  "com.github.cb372"           %% "scalacache-caffeine"    % "0.26.0",
  "org.zalando"                %% "grafter"                % "2.3.0",
  "ch.qos.logback"             % "logback-classic"         % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"          % "3.7.2",
  "org.scalactic"              %% "scalactic"              % "3.0.5",
  "org.scalatest"              %% "scalatest"              % "3.0.5" % "test,it",
  "com.typesafe.akka"          %% "akka-testkit"           % "2.5.18" % "test,it",
  compilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
  compilerPlugin("org.scalamacros" %% "paradise"       % "2.1.1" cross CrossVersion.full)
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings
    // other settings here
  )
