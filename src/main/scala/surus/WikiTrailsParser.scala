package surus

import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object WikiTrailsParser extends App {
  Jsoup.connect("http://www.wikitrail.org/sections/view/at/3011/us-321-to-damascus")
    .get
    .select("[^data-role]").asScala
    .map(element => element.wholeText())
    .filter(_.trim.startsWith("Sleep"))
    .map { wholeText =>
      wholeText
        .split("\n").toList
        .map(_.trim).filter(_.nonEmpty)
        .drop(1)
        .map(parseWaypoint)
    }
    .tap(x => println(s"WikiTrailsParser.scala:11  $x"))
  case class Waypoint(mile: Double, name: String, distanceOffTrail: Option[String])

  //426.8: Lake House [2.0W]
  def parseWaypoint(wp: String): Waypoint = {
    Waypoint(
      mile = wp.takeWhile(_ != ':').toDouble,
      name = wp.dropWhile(_ != ':').drop(2).takeWhile(_ != '[').trim,
      distanceOffTrail = {
        val raw = wp.dropWhile(_ != '[').drop(1).takeWhile(_ != ']').trim
        if (raw.nonEmpty) Some(raw) else None
      }
    )
  }
}
