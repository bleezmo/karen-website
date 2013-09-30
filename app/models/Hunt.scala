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

case class Hunt (date: Date = new Date(), groups:Seq[Group] = Seq(), active:Boolean = true, id: ObjectId = new ObjectId())
object Hunt extends ModelCompanion[Hunt,ObjectId]{
  val dao = new SalatDAO[Hunt, ObjectId](collection = db("hunts")) {}

  def newHunt:Hunt = {
    closeActiveHunt
    Hunt.findOneById(Hunt.insert(Hunt()).get).get
  }

  def getActiveHunt:Option[Hunt] = Hunt.findOne(MongoDBObject("active" -> true))
  def closeActiveHunt = {
    Hunt.update(MongoDBObject("active" -> true),MongoDBObject("active" -> false),false,false)
  }

  implicit val huntReads:Reads[Hunt] = (
    (JsPath \ "groups").read[Seq[Group]]
  ).map(groups => Hunt(new Date(), groups))

  implicit val creatureWrites:Writes[Hunt] = new Writes[Hunt] {
    def writes(hunt: Hunt): JsValue = {
      Json.obj("id" -> hunt.id.toString, "date" -> hunt.date.toString, "groups" -> hunt.groups)
    }
  }
}
