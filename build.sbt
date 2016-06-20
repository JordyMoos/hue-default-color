
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.5.3"
libraryDependencies += "com.twitter" %% "finagle-http" % "6.35.0"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.2"

lazy val root = (project in file(".")).
  settings(
    name := "hue-default-color",
    version := "0.0.1",
    scalaVersion := "2.11.7"
  )
