ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "threadpooltest"
  )

libraryDependencies ++= Seq(
  "org.skinny-framework" %% "skinny-orm"      % "4.0.0",
  "org.postgresql"       % "postgresql"       % "42.5.4",

  "ch.qos.logback"       %  "logback-classic" % "1.2.3"
)