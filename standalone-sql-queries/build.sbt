val scala3Version = "3.2.0-RC1-bin-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "standalone-sql-queries",
    version := "0.1.0-SNAPSHOT",

    scalacOptions ++= Seq("-Ycc"),

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.lihaoyi" %% "pprint" % "0.7.3",
      "org.postgresql" % "postgresql" % "42.3.4"
    )
  )
