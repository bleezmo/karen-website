package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Session(itemId: ObjectId, answered: Boolean)
object Session{
  implicit val sessionWrites:Writes[Session] = new Writes[Session] {
    def writes(s: Session): JsValue = {
      Json.obj("itemId" -> s.itemId.toString, "answered" -> s.answered)
    }
  }
}
case class Group (name:String, sessions:Seq[Session], participants:Seq[Participant], id: ObjectId = new ObjectId())
object Group extends ModelCompanion[Group,ObjectId]{
  val dao = new SalatDAO[Group, ObjectId](collection = mongoCollection("groups")) {}

  implicit val groupReads:Reads[Group] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "participants").read[Seq[Participant]]
  )((name,participants) => Group(name, Seq(), participants))

  implicit val groupWrites:Writes[Group] = new Writes[Group] {
    def writes(g: Group): JsValue = {
      Json.obj("id" -> g.id.toString, "name" -> g.name, "sessions" -> g.sessions, "participants" -> g.participants)
    }
  }
}
