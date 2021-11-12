package surus

import ImplicitConversions._

object Run extends App {
  Grapher
    //.graphTrip(Trip(start = 564.7, end = 656.1))
    .graphTrip(Trip(start = 932.8, end = 983.5))
    .into(excelFormat)
    .tap(x => println(s"ElevationParser.scala:6: $x"))

  def excelFormat(tps: Seq[TrailPoint]): String = {
    tps
      .map(tp => s"${tp.miles},${tp.altitude}")s
      .+:("Mile,Altitude")
      .mkString("\n")
  }
}

// 564.7 VA 625
// 579.9 Jenkins Shelter
// 593.4 Helveys Mill Shelter
// 609.6 Trent's
// 624.8 Wood's Hole Hostel
// 633 Flavor Country Camp
// 643.2 Rice Field Shelter - bad water, water campsite 1.6 further
// 656.1 VA 635
case class TrailPoint(miles: Double, altitude: Double)
case class Trip(start: Double, end: Double) {
  def includes(tp: TrailPoint): Boolean = tp.miles >= Math.min(start, end) && tp.miles <= Math.max(start, end)
}
object Grapher {
  def graphTrip(trip: Trip) = {
    Db.trailPoints.filter(trip.includes).into(normalize)
  }

  def normalize(tps: Seq[TrailPoint]): Seq[TrailPoint] = {
    val start = tps.head.miles
    val low = tps.map(_.altitude).min
    tps.map(tp => TrailPoint(tp.miles - start, tp.altitude - low))
  }
}
object Db extends App {
  import java.io._
  import scala.io.Source

  lazy val db = new File("/Users/adam/projects/surus/allMiles.txt")

  private def parse(tp: String) = {
    val Array(miles, altitude) = tp.split(",").map(_.trim)
    TrailPoint(miles.toDouble, altitude.toInt)
  }
  lazy val trailPoints: Seq[TrailPoint] = Source.fromFile(db).getLines.map(parse).toList
  def save() = {
    def f(d: Double, scale: Int) = BigDecimal(d).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble
    //(3016 to 3016)
    (3001 to 3044)
      .flatMap(ElevationParser.parse)
      .map(tp => s"${f(tp.miles, 2)}, ${tp.altitude.toInt}")
      .mkString("\n")
      .tap(write)

    def write(allMiles: String) = {

      val pw = new PrintWriter(db)
      pw.write(allMiles)
      pw.close
    }
  }
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

case class RelativeTrailPoint(relativeMiles: Double, relativeAltitude: Double, private val tp: TrailPoint) {
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
  import scala.xml.XML
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

    //(high = AltitudeToY(y = 28.0,1000.0),low = AltitudeToY(y = 2.0,4000.0))
    case class Point(x: Double, y: Double)
    def mile(x: Double): Double = (south.mile + ((x - south.x) / (north.x - south.x) * (north.mile - south.mile)))
    def alt(y: Double): Double = (low.alt + (((low.y - y) / (low.y - high.y)) * (high.alt - low.alt))) //.tap(x => println(s"ElevationParser.scala:124: $y -> $x"))

    //
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

