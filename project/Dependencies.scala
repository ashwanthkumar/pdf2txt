import sbt._

object Dependencies {

  val akkaHttpVersion = "10.1.7"
  val akkaStreamVersion = "2.5.21"
  val akkaStack = Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaStreamVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  )

  val logbackVersion = "1.1.7"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVersion
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val loggingStack = Seq(logbackCore, logbackClassic, slf4jApi, scalaLogging)

  val scaldingArgs = "com.twitter" %% "scalding-args" % "0.17.4"
  val jsoup = "org.jsoup" % "jsoup" % "1.13.1"

  val pdfbox = "org.apache.pdfbox" % "pdfbox" % "2.0.15"
  val pdfStack = Seq(pdfbox)

  val lang3 = "org.apache.commons" % "commons-lang3" % "3.1"

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  val mockito = "org.mockito" % "mockito-core" % "2.26.0" % Test
  val testStack = Seq(mockito, scalaTest)

  val rootDependencies = akkaStack ++ Seq(scaldingArgs, lang3, jsoup) ++ loggingStack ++ pdfStack ++ testStack
}
