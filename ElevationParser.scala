package surus

import scala.xml.XML
import pimpathon.any._

object PlotTrip extends App {
  val caledoniaTo233 = ElevationParser.parse(1082.7, 1101.5)
  val hightopToLoft = ElevationParser.parse(903.4, 891.2)
  println(s"ElevationParser.scala.9: ${formatForPlot(Seq(caledoniaTo233, hightopToLoft))}")
  // val frontRoyalStart = 969.1
  // val shen = {
  //   val start = frontRoyalStart
  //   val shen = (3019 to 3020)
  //     .flatMap(ElevationParser.parse)
  //     .sortBy(_.miles * -1)
  //     .filter(tp => tp.miles < start)
  //   val low = shen.head.altitude
  //   shen
  //     .map(tp => TrailPoint(miles = start - tp.miles, altitude = tp.altitude - low))
  //     .map(tp => s"[${tp.miles}, ${tp.altitude}, null]")
  // }

  // val frontRoyal = {
  //   val start = frontRoyalStart
  //   val tps = ElevationParser.parse(3021).filter(tp => tp.miles > start)
  //   val low = tps.head.altitude
  //   tps
  //     .map(tp => TrailPoint(miles = tp.miles - start, altitude = tp.altitude - low))
  //     .map(tp => s"[${tp.miles}, null, ${tp.altitude}]")
  // }

  def formatForPlot(trips: Seq[Seq[RelativeTrailPoint]]): String = trips
    .zipWithIndex
    .flatMap { case (trip, currTripIndex) => 
      trip.map(tp => s"[${tp.relativeMiles}, " + (0 until trips.length).map(tripIndex => if (tripIndex == currTripIndex) tp.relativeAltitude.toString else "null").mkString(",") + "]") 
    }
    .mkString("", ",", "")
  // //https://jsfiddle.net/z4ujdbp9/
  // (shen ++ frontRoyal)
  //   .tap(tps => println()
}

case class RelativeTrailPoint(relativeMiles: Double, relativeAltitude: Double, private val tp: ElevationParser.TrailPoint) {
  val altitude = tp.altitude
  val mileMaker = tp.miles
}

object ElevationParser {
  case class Section(id: Int, name: String, startAtMile: Double)
  val sections = Seq(
    Section(3001, "Springer", 0.0),
    Section(3015, "Pearisburg to Catawba", 633.5),
    Section(3016, "Catawba to Daleville", 707.8),
    Section(3017, "Daleville to James", 728.0),
    Section(3018, "James to Waynesboro", 784.6),
    Section(3019, "South Shenandoah", 861.6),
    Section(3020, "North Shenandoah", 931.2),
    Section(3021, "Front Royal to Harper's Ferry", 969.6),
    Section(3022, "Harper's Ferry to Pine Grove", 1023.2),
    Section(3044, "Baxter", 2174.1) //
    )

  def parse(start: Double, end: Double): Seq[RelativeTrailPoint] = {
    val northernmost = Math.max(start, end)
    val south = Math.min(start, end)
    val tps: Seq[TrailPoint] = sections
      .filterNot(section => northernmost < section.startAtMile)
      .tap(x => println(s"ElevationParser.scala:63: $x"))
      .flatMap(section => parse(section.id))
      .filter(tp => tp.miles >= south && tp.miles <= northernmost)
      .sortBy(tp => tp.miles * Math.signum(end - start).toInt)

    val low = tps.map(_.altitude).min
    tps.map(tp => RelativeTrailPoint(relativeMiles = Math.abs(tp.miles - start), relativeAltitude = Math.abs(tp.altitude - low), tp))
  }
  case class TrailPoint(miles: Double, altitude: Double)
  private def parse(sectionId: Int): Seq[TrailPoint] = {
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
      .map {
        case (x, y) =>
          TrailPoint(miles = mile(x), altitude = alt(y))
      }
  }
}

