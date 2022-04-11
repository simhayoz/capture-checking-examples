val scala3Version = "3.1.3-RC1-bin-SNAPSHOT"

val http4sVersion = "0.23.11"

lazy val root = project
  .in(file("."))
  .settings(
    name := "http4s-websockets",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

//    scalacOptions ++= Seq("-Ycc"),

    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % "0.14.1",
      "com.lihaoyi" %% "scalatags" % "0.11.1")
  )
