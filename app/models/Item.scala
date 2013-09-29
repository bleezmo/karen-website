package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._

case class Item (clue: String, answer: String)
object Item extends ModelCompanion[Item,ObjectId]{
  val dao = new SalatDAO[Item, ObjectId](collection = mongoCollection("items")) {}
}