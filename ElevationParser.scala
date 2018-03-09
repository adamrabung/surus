package surus

import scala.xml.XML
import ImplicitConversions._

object SaveData extends App {
  
  def f(d: Double, scale: Int) = BigDecimal(d).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble
  (3011 to 3044).flatMap(ElevationParser.parse).map(tp => s"[${f(tp.miles, 5)}, ${f(tp.altitude, 2)}]").mkString("[", ",", "]").tap(x => println(s"ElevationParser.scala:7: $x"))
}
object PlotTrip extends App {
  val caledoniaTo233 = ElevationParser.parse(1082.7, 1101.5)
  val hightopToLoft = ElevationParser.parse(903.4, 891.2)
  val upAppleOrchard = ElevationParser.parse(752.3, 774.4)
  val upThePriest = ElevationParser.parse(833.8, 820.1)
  val loftToTurkWithJen = ElevationParser.parse(891.2, 873.4)
  val catawbaWithJen = ElevationParser.parse(707.5, 686.7)

  val ripRapParkingToAfton = ElevationParser.parse(878.7, 861.7) //869.3 calf mtn shelter 9.4 and 7.6
  val caledoniaToPa16 = ElevationParser.parse(1082.7, 1066.6)
  println(s"ElevationParser.scala.9: ${formatForPlot(Seq(ripRapParkingToAfton, caledoniaToPa16))}")
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
    .flatMap {
      case (trip, currTripIndex) =>
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

object ImplicitConversions {
  implicit class RichU[A](val a: A) extends AnyVal {
    def tap(block: A => Unit): A = { block(a); a }
    def into[B](f: A => B): B = f(a)
    // def where(pred: A => Boolean): Option[A] = if (pred(a)) Some(a) else None
    // def where(cond: Boolean): Option[A] = if (cond) Some(a) else None
    // def equalsAny[B](bs: B*)(implicit equality: Equality[A, B]): Boolean = bs.exists(b => b == a)
    // def notEqualsAny[B](bs: B*)(implicit equality: Equality[A, B]): Boolean = bs.forall(b => b != a)
  }
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
      .flatMap(n => n.map(_.trim.split(" ").map(_.toDouble).toSeq.into { case Seq(x, y) => (x, y) }))
      .map {
        case (x, y) =>
          TrailPoint(miles = mile(x), altitude = alt(y))
      }
  }
}

