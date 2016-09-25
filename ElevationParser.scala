package surus

import scala.xml.XML
import pimpathon.any._

//https://jsfiddle.net/z4ujdbp9/
object ElevationParser extends App {
  //springer - 3001
  //baxter - 3044
  //shenandoah 3019 and 3020

  var frontRoyalStart = 969.1
  val route50 = 989.1
  val hightopParking = 905.8
  val shen = {
    val start = frontRoyalStart
    val shen = (3019 to 3020)
      .flatMap(parse)
      .sortBy(_.miles * -1)
      .filter(tp => tp.miles < start)
    val low = shen.map(_.altitude).min
    shen
      .map(tp => TrailPoint(miles = start - tp.miles, altitude = tp.altitude - low))
      .map(tp => s"[${tp.miles}, ${tp.altitude}, null]")
  }

  val frontRoyal = {
    val start = frontRoyalStart
    val tps = parse(3021)
      .filter(tp => tp.miles > start)
    val low = tps.map(_.altitude).min
    tps.map(tp => TrailPoint(miles = tp.miles - start, altitude = tp.altitude - low))
    .map(tp => s"[${tp.miles}, null, ${tp.altitude}]")
  }

  (shen ++ frontRoyal)
    .tap(tps => println(s"ElevationParser.scala:14 ${tps.mkString("[", ",", "]")}"))

  case class TrailPoint(miles: Double, altitude: Double)
  def parse(sectionId: Int): Seq[TrailPoint] = {
    val section = XML.load("http://www.wikitrail.org/sections/profile/at/" + sectionId)
    val altituteLines = (section \\ "line" \\ "@y1")
    val (altitueTexts, distanceTexts) = (section \\ "text").splitAt(altituteLines.size)

    case class AltitudeToY(y: Double, alt: Double)
    val (high, low) = {
      val altInfo: Seq[AltitudeToY] = altituteLines
        .zip(altitueTexts.map(_.text)) //List((28.0,0), (21.5,1000), (15.0,2000), (8.5,3000), (2.0,4000))
        .map { case (y, alt) => AltitudeToY(y.toString.toDouble, alt.toDouble) }
        .sortBy(_.y) //   AltitudeToY(21.5,3000.0), AltitudeToY(15.0,4000.0), AltitudeToY(8.5,5000.0), AltitudeToY(2.0,6000.0)
      (altInfo.head, altInfo.last)
    }

    case class MileToX(x: Double, mile: Double)
    val (south, north) = {
      val distanceXs = distanceTexts.map(_.text.toDouble).sorted
      (MileToX(0, distanceXs.head), MileToX(100, distanceXs.last))
    }

    case class Point(x: Double, y: Double)
    def mile(x: Double): Double = (south.mile + ((x - south.x) / (north.x - south.x) * (north.mile - south.mile)))
    def alt(y: Double): Double = (low.y - y) / (low.y - high.y) * high.alt
    section
      .\\("@points")
      .map(n => n.toString.split(","))
      .flatMap(n => n.map(_.trim.split(" ").map(_.toDouble).toSeq.|> { case Seq(x, y) => (x, y) }))
      .map { case (x, y) => TrailPoint(mile(x), alt(y)) }
  }
}
