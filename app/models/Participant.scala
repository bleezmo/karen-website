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

case class Participant (name: String, number: String, id: ObjectId = new ObjectId())
object Participant extends ModelCompanion[Participant,ObjectId]{
  val dao = new SalatDAO[Participant, ObjectId](collection = db("participants")) {}

  implicit val participantReads:Reads[Participant] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "number").read[String]
  )((name,number) => Participant(name,number))

  implicit val participantWrites:Writes[Participant] = new Writes[Participant] {
    def writes(p: Participant): JsValue = {
      Json.obj("name" -> p.name, "number" -> p.number, "id" -> p.id.toString)
    }
  }
}
