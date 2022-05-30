val scala3Version = "3.2.0-RC1-bin-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "standalone-websockets",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    scalacOptions ++= Seq("-Ycc"), // , "-feature", "-deprecation"

    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "0.7.29" % Test),

    Compile / run / fork := true // Ensure port is closed to be able to run again
  )
