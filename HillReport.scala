package surus

import scala.io.Source
import scala.util.chaining._

//object HillReport extends App {
//  val start = 370
//  val end = 470
//  val nobo = true
//  val sobo = false
//  val distanceChecks = (1 to 50).map(_.toDouble / 10)
//  //val distanceChecks = Seq(10.1)
//
//
//  case class Delta(start: Point, end: Point) {
//    val distance = math.abs(end.mile - start.mile)
//    val altDelta = end.alt - start.alt
//    val slope = altDelta / distance
//    override val toString = s"[${if (start.mile < end.mile) "nobo" else "sobo"}: start = $start, end = $end, Δm = $distance, Δa = $altDelta, grade = ${slope}]"
//  }
//
//  def deltas(points: Seq[Point]) = {
//    points
//      .sliding(20)
//      .flatMap { window =>
//        val start = window.head
//        window.tail.map(end => Delta(start, end))
//      }
//      .toList
//  }
//
//  val all =
//    (if (nobo) deltas(noboPoints) else Seq.empty) //.++(deltas(noboPoints.reverse))
//      .++((if (sobo) deltas(noboPoints.reverse) else Seq.empty))
//  //.tap(x => println(s"${x.mkString("\n")}"))
//
//  distanceChecks.map { maxDist =>
//    all
//      .filter(p => p.distance < maxDist && p.distance > 3)
//      //.tap(x => println(s"HillReport.scala:35 $maxDist => ${x.length} "))
//      //.minByOption(_.altDelta)
//      .sortBy(_.slope.abs)
//      .distinctBy(_.start)
//      .take(25).mkString("\n\t")
//      .tap(x => println(s"HillReport.scala:10\n $maxDist: $x "))
//  }
//}
//
//case class Mile(value: Double)
//
//case class Altitude(value: Int)
//
//case class Point(mile: Mile, alt: Altitude)
//
//object Point {
//  private lazy val noboPoints = Source
//    .fromFile("/Users/adam/projects/surus/allMiles.txt")
//    .getLines
//    .toSeq
//    .map { line =>
//      line.split(", ") match {
//        case Array(mile, alt) => Point(Mile(mile.toDouble), Altitude(alt.toInt))
//      }
//    }
//    .toList
//
//  def points(start: Mile, end: Mile) =
//    allPoints.filter(point => point.mile > start && point.mile < end)
//
//}
