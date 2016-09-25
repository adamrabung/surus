lazy val root = (project in file(".")).
  settings(
    resolvers += "jcenter" at "http://jcenter.bintray.com",
    resolvers += "Stacy Curl's repo" at "http://dl.bintray.com/stacycurl/repo/",
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies += "org.scala-lang" % "scala-xml" % "2.11.0-M4",
    libraryDependencies += "com.github.stacycurl" %% "pimpathon" % "1.7.0"
  )
