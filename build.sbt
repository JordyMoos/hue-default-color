
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.5.3"

lazy val root = (project in file(".")).
  settings(
    name := "hue-default-color",
    version := "0.0.1",
    scalaVersion := "2.11.7"
  )
