import Dependencies._

val buildVersion = sys.env.getOrElse("GO_PIPELINE_LABEL", "0.1.0-SNAPSHOT")

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := buildVersion
ThisBuild / organization     := "in.ashwanthkumar"
ThisBuild / organizationName := "pdf2txt"

lazy val root = (project in file("."))
  .settings(
    name := "pdf2txt",
    libraryDependencies ++= rootDependencies,
    resolvers += Resolver.mavenLocal
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.


