val scala3Version = "3.2.0-RC1-bin-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "bug-examples",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    scalacOptions ++= Seq("-Ycc"),

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )
