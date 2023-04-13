package surus

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DailyMiles extends App {
  //allPaths(point("Yellow Creek Road"), point("Max Patch Parking"), maxDays = 5).foreach(route => println(format(route) + "\n\n\n"))
  allPaths(point("Fontana Dam Visitor Center"), point("Max Patch Parking"), minDay = 10, maxDay = 16, maxLastDay = 10).foreach(route => println(format(route, LocalDate.of(2023, 4, 30)) + "\n\n\n"))

  def allPaths(start: TrailPoint, stop: TrailPoint, minDay: Int, maxDay: Int, maxLastDay: Int): Seq[Seq[TrailPoint]] = {
    paths(start, stop, allPoints, direction = math.signum(stop.mile.toInt - start.mile.toInt), minDay = minDay, maxDay = maxDay, maxLastDay = maxLastDay)
      .filter(path => path.contains(stop))
    //.filter(_.length == maxDays + 1) //x days + the 2 termini
  }

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

  private def paths(start: TrailPoint, end: TrailPoint, allPoints: Seq[TrailPoint], direction: Int, minDay: Int, maxDay: Int, maxLastDay: Int): Seq[Seq[TrailPoint]] = {
    if (end.mile - start.mile < maxLastDay) {
      Seq(Seq(start, end))
    } else {
      allPoints
        .dropWhile(p => p.mile - start.mile <= minDay)
        .takeWhile(p => p.mile - start.mile <= maxDay && p != end)
        //.tap(x => println(s"DailyMiles.scala:36 $start => $x"))
        .flatMap { next =>
          val pathsFromHere = paths(start = next, end = end, allPoints = allPoints, direction = direction, minDay = minDay, maxDay = maxDay, maxLastDay = maxLastDay)
          if (pathsFromHere.nonEmpty) {
            pathsFromHere.map(pathFromHere => Seq(start) ++ pathFromHere)
          }
          else {
            Seq(Seq(start, next))
          }
        }
    }
  }

  private lazy val pointForName = allPoints.map(p => p.name.trim -> p).toMap

  def point(name: String) = pointForName.get(name).getOrElse(sys.error(s"no point found for name $name: ${pointForName.keys.mkString("\n")}"))

  lazy val allPoints = {
    """
      |158.4	Yellow Creek Road
      |165.9  Fontana Dam Shelter
      |166.3  Fontana Dam Visitor Center
      |172.3  Campsite
      |177.7	Mollies Ridge
      |180.8	Russell Field Shelter
      |183.7	Spence Field Shelter
      |189.8	Derrick Knob Shelter
      |195.5	Silers Bald Shelter
      |197.2	Double Spring Gap Shelter
      |202.8	Mt Collins Shelter
      |207.7	Newfound Gap
      |210.8	Icewater Spring
      |218.2	Pecks Corner
      |223.4	Tri-Corner Knob
      |231.1	Cosby Knob
      |238	Davenport Gap Shelter
      |241.8	Standing Bear Hostel
      |243.9	Painter Branch Campsite
      |248.7	Groundhog Creek
      |254.4	Max Patch Parking
      |257	Roaring Fork
      |260.5	Lemon Gap
      |261.8	Walnut Mountain
      |263.2	campsite atop small knoll
      |268.3	Garenflo Gap/1173
      |271.7	Deer Park Mountain
      |274.9	Hot Springs
      |279.1	Campsite
      |283.2	Campsite
      |285.9	Spring Mountain
      |294.5	Little Laurel
      |301.8	Jerry Cabin
      |308.5	Flint Mountain
      |315.1	Campsite
      |317.3	Hogback Ridge
      |319.7	Sam's Gap
      |"""
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
    override lazy val toString = s"[$name ($mile)]"
  }
}