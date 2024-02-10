ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "vote",
    version := "0.0.1",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.2.10",
        "com.typesafe.akka" %% "akka-stream" % "2.6.16",
        "org.mongodb.scala" %% "mongo-scala-driver" % "4.4.0",
        "de.heikoseeberger" %% "akka-http-json4s" % "1.37.0",
        // Дополнительные библиотеки JSON, если необходимо
        "org.json4s" %% "json4s-native" % "4.0.3",
        "org.json4s" %% "json4s-jackson" % "4.0.3",
        "com.typesafe.akka" %% "akka-actor" % "2.6.16",

        // SLF4J API
        "org.slf4j" % "slf4j-api" % "1.7.36", // Используйте последнюю версию

        // Реализация SLF4J (Logback)
        "ch.qos.logback" % "logback-classic" % "1.4.7", // Используйте последнюю версию

        "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10"

      )
)
