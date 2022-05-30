val scala3Version = "3.2.0-RC1-bin-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "cask-websockets",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    scalacOptions ++= Seq("-Ycc"), // , "-Xprint:all"

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.lihaoyi" %% "cask" % "0.8.0",
      "com.lihaoyi" %% "scalatags" % "0.11.1")
  )
