package surus

import surus.DailyMiles.{allPoints, point}
import surus.ImplicitConversions.RichU

import java.time.LocalDate
import java.time.format.DateTimeFormatter


object DailyMiles extends App {
  //allPaths(point("Fontana Dam Visitor Center"), point("Max Patch Parking"), minDay = 10, maxDay = 16, maxLastDay = 10).foreach(route => println(format(route, LocalDate.of(2023, 4, 30)) + "\n\n\n"))
  allPaths(
    start = point("Undermountain Road/Salisbury CT"),
    end = point("Silver Hill Campsite"),
    //start = point("Bear Mountain"),
    minDay = 10, maxDay = 20, maxLastDay = 8)
    .foreach(route => println(format(route, LocalDate.of(2023, 9, 23)) + "\n\n\n"))

  def allPaths(start: TrailPoint, end: TrailPoint, minDay: Int, maxDay: Int, maxLastDay: Int): Seq[Seq[TrailPoint]] = {
    paths(start, end, adjustAllPoints(start, end, allPoints), minDay = minDay, maxDay = maxDay, maxLastDay = maxLastDay)
     //r .filter(path => path.contains(end))
  }

  def paths(curr: TrailPoint, end: TrailPoint, allPoints: Seq[TrailPoint], minDay: Int, maxDay: Int, maxLastDay: Int): Seq[Seq[TrailPoint]] = {
    if (curr == end) {
      Seq(Seq(end))
    } else {
      allPoints.filter { p =>
        val d = curr.distance(p)
        d > 0 && d >= minDay && d <= maxDay
      }
        .flatMap { p =>
          paths(curr = p, end = end, allPoints = allPoints.dropWhile(_ != p).drop(1), minDay = minDay, maxDay = maxDay, maxLastDay = maxLastDay)
        }
    }
  }

  case class Trip(start: TrailPoint, end: TrailPoint, all: Seq[TrailPoint])

  def adjustAllPoints(start: TrailPoint, end: TrailPoint, allPoints: Seq[TrailPoint]): Seq[TrailPoint] = {
    val direction = if (end.mile >= start.mile) 1 else -1
    val (low, high) = if (direction == 1) (start, end) else (end, start)
    allPoints
      .dropWhile(_ != low)
      .takeWhile(_ != high)
      .:+(high)
      .sortBy(_.mile * direction)
      .into { ps =>
        ps.map(p => p.copy(mile = math.abs(p.mile - ps.head.mile)))
      }
  }


  //  private def paths(start: TrailPoint, end: TrailPoint, allPoints: Seq[TrailPoint], minDay: Int, maxDay: Int, maxLastDay: Int): Seq[Seq[TrailPoint]] = {
  //    // Determine the direction: 1 for northbound, -1 for southbound
  //    val direction = if (end.mile >= start.mile) 1 else -1
  //
  //    def validNextPoints(current: TrailPoint): Seq[TrailPoint] = {
  //      val potentialNextPoints = allPoints
  //        .dropWhile(_ != current)
  //        .tap(x => println(s"DailyMiles.scala:30 dropped $x"))
  //        .takeWhile { point =>
  //          val dist = direction * (point.mile - current.mile)
  //          println(s"taking $current => $point = $dist $point $end")
  //          point == current ||
  //            (dist >= minDay && dist <= (if (point == end) maxLastDay else maxDay))
  //        }
  //        .tap(x => println(s"DailyMiles.scala:36 taked $x"))
  //      potentialNextPoints.sortBy(_.mile * direction).tap(x => println(s"DailyMiles.scala:49 validNextPoints  $x"))
  //    }
  //
  //    def findPaths(current: TrailPoint, currentPath: Seq[TrailPoint]): Seq[Seq[TrailPoint]] = {
  //      if (current == end) {
  //        Seq(currentPath :+ end)
  //      } else {
  //        val nextPoints = validNextPoints(current)
  //        if (nextPoints.isEmpty)
  //          Seq()
  //        else
  //          nextPoints.flatMap(point => findPaths(point, currentPath :+ point))
  //      }
  //    }
  //
  //    findPaths(current = start, currentPath = Seq(start)).tap(x => println(s"DailyMiles.scala:63 $start, $end => $x"))
  //  }

  //  private def paths(start: TrailPoint, end: TrailPoint, allPoints: Seq[TrailPoint], minDay: Int, maxDay: Int, maxLastDay: Int): Seq[Seq[TrailPoint]] = {
  //    if (start.distance(end) < maxLastDay) {
  //      println(s"done: ${Seq(start, end)} => ${start.distance(end)}")
  //      Seq(Seq(start, end))
  //    } else {
  //      println(s"a $start: $allPoints")
  //      allPoints
  //        .filter { p =>
  //          val dist = start.distance(p)
  //          (dist >= minDay && dist <= maxDay).tap(x => println(s"DailyMiles.scala:44 is $p ($dist) in range of $start? $x"))
  //        }
  //        .tap(x => println(s"DailyMiles.scala:44  $x"))
  //        .flatMap { next =>
  //          val pathsFromHere = paths(start = next, end = end, allPoints = allPoints, minDay = minDay, maxDay = maxDay, maxLastDay = maxLastDay)
  //          if (pathsFromHere.nonEmpty) {
  //            pathsFromHere.map(pathFromHere => Seq(start) ++ pathFromHere)
  //          }
  //          else {
  //            Seq(Seq(start, next))
  //          }
  //        }
  //    }
  //  }
  private def format(route: Seq[TrailPoint], startDate: LocalDate): String = {
    route.sliding(2).toList
      .zip(Iterator.iterate(startDate)(_.plusDays(1)).map(d => d.format(DateTimeFormatter.ofPattern("EEE M-d"))))
      .map { case (Seq(p1, p2), day) => s"$day: ${p1.name}-${p2.name} (${(p2.mile - p1.mile).abs.formatted("%2.1f")})" }
      .mkString("\n")
      .+ {
        val total = math.abs(route.head.mile - route.last.mile)
        val days = route.length - 1
        val max = route.sliding(2).map { case Seq(p1, p2) => p2.mile - p1.mile }.max.formatted("%2.1f")
        s"\nTotal: ${total.abs.formatted("%2.1f")}, days: $days, max: ${max}, average: ${(total.abs / days).formatted("%2.1f")}"
      }
  }


  private lazy val pointForName = allPoints.map(p => p.name.trim -> p).toMap

  def point(name: String) = pointForName.get(name).getOrElse(sys.error(s"no point found for name $name: ${pointForName.keys.mkString("\n")}"))

  lazy val allPoints = {
    """
      |1404.1	Bear Mountain
      |1408.9	Hemlock Springs Campsite
      |1426.9	Clarence Fahenstock State Park
      |1430.6	Shenandoah Tenting Area
      |1431.9	RPH Shelter
      |1440.9	Morgan Stewart Shelter
      |1448.7	Telepone Pioneers Shelter
      |1451.8	Pawling/Wingdale NY
      |1457.5	Wiley Shelter
      |1461.5	Ten Mile River Shelter
      |1467	Schagticoke Mountain Campsite
      |1469.9	Mt Algo Shelter
      |1477.2	Stewart Hollow Brook Shelter
      |1477.8	Stony Brook Campsite
      |1480.5	Silver Hill Campsite
      |1483.9	Caesar Brook Campsite
      |1487.2	Pine Swamp Brook Shelter
      |1489.6	Sharon Mountain Campsite
      |1492.5	Belters Campsite
      |1498.5	Limestone Spring Shelter
      |1502.9	Undermountain Road/Salisbury CT
      """
      .stripMargin.trim
      .split("\n").toSeq
      .map { line =>
        line.split("\\s+", 2) match {
          case Array(mileString, name) => TrailPoint(mileString.toDouble, name.trim)
          case x => sys.error(x.toList.toString)
        }
      }
  }

  case class TrailPoint(mile: Double, name: String) {
    def distance(other: TrailPoint): Double = {
      other.mile - mile
    }

    override lazy val toString = s"[$name ($mile)]"
  }
}

