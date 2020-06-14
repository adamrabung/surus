package analysis

import scala.io.Source
import util.ImplicitConversions._
import util.ControlStructures._

object HillReport extends App {
  case class Point(mile: Double, alt: Int)
  case class Delta(start: Point, end: Point) {
    val distance = math.abs(end.mile - start.mile)
    val altDelta = end.alt - start.alt
    override val toString = s"[${if(start.mile < end.mile) "nobo" else "sobo"}: start = $start, end = $end, Δm = $distance, Δa = $altDelta, grade = ${altDelta / distance}]"
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
    .filter(point => point.mile > 550 && point.mile < 1230)
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

  val all = deltas(noboPoints).++(deltas(noboPoints.reverse))   
  (1 to 50).map(_.toDouble / 10).map { maxDist =>
    all
      .filter(_.distance < maxDist)
      .tap(x => println(s"HillReport.scala:35 $maxDist => ${x.length} "))
      .optMaxBy(_.altDelta)
      .tap(x => println(s"HillReport.scala:10 max = $maxDist: $x "))
  }
}
