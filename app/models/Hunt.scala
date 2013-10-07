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

case class Hunt (date: Date = new Date(), groups:Seq[ObjectId] = Seq(), active:Boolean = true, id: ObjectId = new ObjectId())
object Hunt extends ModelCompanion[Hunt,ObjectId]{
  val dao = new SalatDAO[Hunt, ObjectId](collection = db("hunts")) {}

  def newHunt:Hunt = {
    closeActiveHunt
    Hunt.findOneById(Hunt.insert(Hunt()).get).get
  }

  def getActiveHunt:Option[Hunt] = Hunt.findOne(MongoDBObject("active" -> true))
  def getActiveGroups:Seq[Group] = getActiveHunt.map(h => h.groups.map(id => Group.findOneById(id)).flatten).getOrElse(Seq())
  def closeActiveHunt = {
    Hunt.update(MongoDBObject("active" -> true),MongoDBObject("active" -> false),false,false)
  }

  implicit val huntReads:Reads[Hunt] = (
    (JsPath \ "active").read[Boolean]
  ).map(active => Hunt(active = active))

  implicit val creatureWrites:Writes[Hunt] = new Writes[Hunt] {
    def writes(hunt: Hunt): JsValue = {
      Json.obj("id" -> hunt.id.toString, "active" -> hunt.active, "date" -> hunt.date.toString, "groups" -> hunt.groups.map(gid => Group.findOneById(gid)).flatten.seq)
    }
  }
}
