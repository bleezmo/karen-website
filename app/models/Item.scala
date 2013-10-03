package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import mongoContext._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Item (clue: String, answer: String, id:ObjectId = new ObjectId())
object Item extends ModelCompanion[Item,ObjectId]{
  val dao = new SalatDAO[Item, ObjectId](collection = db("items")) {}

  implicit val itemReads = (
    (__ \ "clue").read[String] and
    (__ \ "answer").read[String]
  )((clue,answer) => Item(clue,answer))

  implicit val itemWrites = new Writes[Item]{
    def writes(o: Item): JsValue = {
      Json.obj("clue" -> o.clue, "answer" -> o.answer, "id" -> o.id.toString)
    }
  }

}