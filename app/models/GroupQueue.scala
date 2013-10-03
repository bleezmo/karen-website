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

case class GroupMessage(groupId: ObjectId, message: String, timestamp:Long = System.currentTimeMillis(), id:ObjectId = new ObjectId())
object GroupMessage{
  implicit val gmWrites = new Writes[GroupMessage] {
    def writes(o: GroupMessage): JsValue = {
      Json.obj("message" -> o.message, "timestamp" -> o.timestamp)
    }
  }
}
case class GroupQueue(groupId: ObjectId, messages: Seq[GroupMessage], id:ObjectId = new ObjectId())
object GroupQueue extends ModelCompanion[GroupQueue,ObjectId]{
  val dao = new SalatDAO[GroupQueue, ObjectId](collection = db("groupQueues")) {}

  def newQueue(groupId:ObjectId) = GroupQueue.insert(GroupQueue(groupId,Seq()))
  def append(groupId:ObjectId, message:String) = {
    GroupQueue.update(
      MongoDBObject("groupId" -> groupId),
      MongoDBObject("$push" -> MongoDBObject("messages" -> grater[GroupMessage].asDBObject(GroupMessage(groupId,message)))),
      false,false
    )
  }
  def getMessages(groupId:ObjectId, cutoff:Option[Long] = None, limit:Int = -1):Seq[GroupMessage] = {
    GroupQueue.findOne(MongoDBObject("groupId" -> groupId)) match {
      case Some(group) => {
        val messages = cutoff.map(timestamp => group.messages.filter(gm => gm.timestamp > timestamp)).getOrElse(group.messages)
        if(limit > -1) messages.foldLeft[Seq[GroupMessage]](Seq())((acc,gm) =>
          if (acc.size < limit) acc :+ gm else acc
        )
        else messages
      }
      case None => Seq()
    }
  }

}
