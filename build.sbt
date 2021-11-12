
// This is an application with a main method
// scalaJSUseMainModuleInitializer := true
// enablePlugins(ScalaJSPlugin)

lazy val root = (project in file(".")).
  settings(
    resolvers += "jcenter" at "https://jcenter.bintray.com",
    name := "surus",
    version := "1.0",
    scalaVersion := "2.13.2",
    libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
  )
