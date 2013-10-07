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
import play.api.Logger

object Direction extends Enumeration{
  val Sent = Value("sent")
  val Received = Value("received")
}
case class GroupMessage(message: String, direction:Direction.Value, timestamp:Long = System.currentTimeMillis())
object GroupMessage{
  implicit val gmWrites = new Writes[GroupMessage] {
    def writes(o: GroupMessage): JsValue = {
      //reverse direction when sending to client because it is opposite from client perspective
      if (o.direction == Direction.Received){
        Json.obj("sent" -> o.message, "received" -> "", "timestamp" -> o.timestamp)
      }else{
        Json.obj("sent" -> "", "received" -> o.message, "timestamp" -> o.timestamp)
      }
    }
  }
}
case class Group (name:String, messages: Seq[GroupMessage], endpoint:String, id: ObjectId = new ObjectId())
object Group extends ModelCompanion[Group,ObjectId]{
  val dao = new SalatDAO[Group, ObjectId](collection = db("groups")) {}

  def addEndpoint(groupName: String, endpoint: String){
    Group.findOne(MongoDBObject("name" -> groupName)) match {
      case Some(group) => update(MongoDBObject("_id" -> group.id),MongoDBObject("$set" -> MongoDBObject("endpoint" -> endpoint)),false,false)
      case None => Group.insert(new Group(groupName,Seq(),endpoint))
    }
  }

  def addMessage(groupId:ObjectId, m:GroupMessage){
    update(MongoDBObject("_id" -> groupId), MongoDBObject("$push" -> MongoDBObject("messages" -> grater[GroupMessage].asDBObject(m))),false,false)
  }

  def getNewMessages(groupId:ObjectId, optcutoff:Option[Long]):Seq[GroupMessage] = {
    findOneById(groupId) match {
      case Some(group) => optcutoff match {
        case Some(cutoff) => group.messages.filter(gm => gm.timestamp > cutoff)
        case None => group.messages
      }
      case None => {
        Logger.error("Group.getNewMessages: no group found given id")
        Seq()
      }
    }
  }

  override def insert(g:Group):Option[ObjectId] = {
    super.insert(g) match {
      case Some(id) => Hunt.getActiveHunt match {
        case Some(hunt) => {
          Hunt.update(MongoDBObject("_id" -> hunt.id), MongoDBObject("$push" -> MongoDBObject("groups" -> id)),false,false)
          Some(id)
        }
        case None => None
      }
      case None => None
    }
  }

  implicit val groupWrites:Writes[Group] = new Writes[Group] {
    def writes(g: Group): JsValue = {
      Json.obj("id" -> g.id.toString, "name" -> g.name, "messages" -> g.messages)
    }
  }
}
