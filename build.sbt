lazy val root = (project in file(".")).
  settings(
    resolvers += "jcenter" at "https://jcenter.bintray.com",
    name := "surus",
    version := "1.0",
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
      "org.jsoup" % "jsoup" % "1.15.3",
    )
  )
