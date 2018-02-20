lazy val root = (project in file(".")).
  settings(
    resolvers += "jcenter" at "http://jcenter.bintray.com",
    resolvers += "Stacy Curl's repo" at "http://dl.bintray.com/stacycurl/repo/",
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.11",
    libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
    libraryDependencies += "com.github.stacycurl" %% "pimpathon" % "1.7.0"
  )
