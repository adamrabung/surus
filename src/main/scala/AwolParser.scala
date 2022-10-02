package surus

object AwolParser extends App {
  val line = "837.3 1355.8 County Rd 565, Glenwood, NJ (1.1W), stream south of road . . . . . . .w 720"
  val Pattern = {
    val mile = """(\d+\.\d)""".r
    val desc = """(.*?)"""
    val dots = """( \.)+"""
    val latLong = """(\d\d\.\d\d\d,\-\d\d\.\d\d\d)?"""
    val props = """([wtviRkvs()\d]*)"""
    val extraDesc = """(.*?)?"""
    val altitude = """(\d+)"""

    //s"""$mile $mile $desc$dots\s*$latLong\s*$props\s*$extraDesc$altitude""".rd
    "too much pain exception"
  }
  //val Pattern(startMile, endMile, desc, dots, latLong, props, garb, alt) = line
  println(s"sss")
}
