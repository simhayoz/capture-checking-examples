val scala3Version = "3.2.0-RC1-bin-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "quill-sql-queries",
    version := "0.1.0-SNAPSHOT",

    scalacOptions ++= Seq("-Ycc"),

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "0.7.29" % Test,
        "com.opentable.components" % "otj-pg-embedded" % "1.0.1",
        "org.postgresql" % "postgresql" % "42.3.4",
        "io.getquill" %% "quill-jdbc" % "3.17.0.Beta3.0-RC2"
    )
  )
