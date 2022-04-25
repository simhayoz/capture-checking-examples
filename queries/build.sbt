val scala3Version = "3.1.3-RC1-bin-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "queries",
    version := "0.1.0-SNAPSHOT",

//    scalacOptions ++= Seq("-Ycc"),

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "0.7.29" % Test,
        "com.opentable.components" % "otj-pg-embedded" % "0.13.1",
        "org.postgresql" % "postgresql" % "42.2.8",
        "io.getquill" %% "quill-jdbc" % "3.17.0.Beta3.0-RC2"
    )
  )
