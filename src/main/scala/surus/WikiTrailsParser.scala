package surus

import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object ParseAllWaypoints extends App {

}
object WikiTrailsParser extends App {
private val sectionIds = 3001 to 3044
  sectionIds.map { sectionId =>
    Jsoup.connect(s"http://www.wikitrail.org/sections/view/at/$sectionId/")
      .get
      .select("span.ui-li-icon").asScala //<span class='ui-li-icon restaurant16' title='Restaurant' width='16' height='16'></span>
      //.tap(x => println(s"WikiTrailsParser.scala:18  ${x.mkString("\n")}")
      .filter(e => e.attr("title").nonEmpty) //"Add Shelter"
      .map(element => (parseWaypoint(element.parent().wholeText().trim, element.attr("title").trim)))
      .distinct
      .sortBy(_.mile)
      .foreach { wp =>
        import wp._
        println(s"$mile, $name, $distanceOffTrail, $wpType")
      }
  }

  case class Waypoint(mile: Double, name: String, distanceOffTrail: Option[String], wpType: String)


  //426.8: Lake House [2.0W]
  def parseWaypoint(wp: String, wpType: String): Waypoint = {
    Waypoint(
      mile = wp.takeWhile(_ != ':').toDouble,
      name = wp.dropWhile(_ != ':').drop(2).takeWhile(_ != '[').trim,
      distanceOffTrail = {
        val raw = wp.dropWhile(_ != '[').drop(1).takeWhile(_ != ']').trim
        if (raw.nonEmpty) Some(raw) else None
      },
      wpType = wpType
    )
  }
}
