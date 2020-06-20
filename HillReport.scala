package surus

import scala.io.Source
import scala.util.chaining._

object HillReport extends App {
  val start = 0
  val end = 1221
  val nobo = true
  val sobo = false
  //val distanceChecks = (1 to 50).map(_.toDouble / 10)
  val distanceChecks = Seq(1.0)

  case class Point(mile: Double, alt: Int)
  case class Delta(start: Point, end: Point) {
    val distance = math.abs(end.mile - start.mile)
    val altDelta = end.alt - start.alt
    val slope = altDelta / distance
    override val toString = s"[${if(start.mile < end.mile) "nobo" else "sobo"}: start = $start, end = $end, Δm = $distance, Δa = $altDelta, grade = ${slope}]"
  }
  val noboPoints = Source
    .fromFile("/Users/adam/projects/surus/allMiles.txt")
    .getLines
    .toSeq
    .map { line =>
      line.split(", ") match {
        case Array(mile, alt) => Point(mile.toDouble, alt.toInt)
      }
    }
    .filter(point => point.mile > start && point.mile < end)
    .toList

  def deltas(points: Seq[Point]) = {
    points
      .sliding(20)
      .flatMap { window => 
        val start = window.head
        window.tail.map(end => Delta(start, end))
      }
      .toList
  }

  val all = 
    (if (nobo) deltas(noboPoints) else Seq.empty)//.++(deltas(noboPoints.reverse))   
    .++((if (sobo) deltas(noboPoints.reverse) else Seq.empty))
  
  distanceChecks.map { maxDist =>
    all
      .filter(p => p.distance < maxDist && p.distance > .25)
      //.tap(x => println(s"HillReport.scala:35 $maxDist => ${x.length} "))
      //.minByOption(_.altDelta)
      .sortBy(_.slope)
      .distinctBy(_.start)
      .take(25).mkString("\n\t")
      .tap(x => println(s"HillReport.scala:10\n $maxDist: $x "))
  }
}
