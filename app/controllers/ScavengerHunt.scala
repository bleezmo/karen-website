package controllers

import play.api.mvc.{Action, Controller}
import models._
import org.bson.types.ObjectId
import play.api.libs.json._
import com.novus.salat.dao.SalatInsertError
import models.mongoContext._
import dispatch._
import concurrent.Await
import scala.concurrent.duration._
import com.ning.http.client.Response
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import java.net.URLEncoder
import com.mongodb.casbah.commons._
import com.typesafe.config.ConfigFactory
import dispatch.Req
import com.novus.salat.dao.SalatInsertError
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess
import scala.Some

object ScavengerHunt extends Controller{
  def index = Action{request=>
    Ok(views.html.scavengerhunt())
  }
  def receiveTexts(cutoff:String) = Action{request =>
    Ok(JsArray(
      Hunt.getActiveHunt.map(hunt =>
        hunt.groups.foldLeft[Seq[JsValue]](Seq())((acc,groupId) => {
          Group.findOneById(groupId) match {
            case Some(group) => {
              acc :+ Json.toJson(group.copy(messages = group.messages.filter(gm => gm.timestamp > cutoff.toLong)))
            }
            case None => {
              Logger.error("no group found in hunt!")
              acc
            }
          }
        })
      ).getOrElse(Seq())
    ))
  }
  def getHunts = Action{request=>
    Ok(JsArray(Hunt.find(MongoDBObject()).foldRight[Seq[JsValue]](Seq())((hunt,acc) => {
      acc :+ Json.toJson(hunt)
    })))
  }
  def nexmoEndpoint = Action{request =>
    Logger.info("received text: "+request.uri)
    val number = request.getQueryString("msisdn")
    val message = request.getQueryString("text")
    if (number.isDefined && message.isDefined && Hunt.getActiveHunt.isDefined){
      message.get match {
        case x if x.toLowerCase.startsWith("start") => {
          val xsplit = x.split(" ")
          if(xsplit.length == 2){
            val name = xsplit(1)
            Group.addEndpoint(name,number.get)
          }
        }
        //add message to queue
        case x => {
          Hunt.getActiveGroups
            .find(g =>{
            g.endpoint == number.get
          }).foreach(group => {
            Group.addMessage(group.id, GroupMessage(x,Direction.Sent))
          })
        }
      }
    } else Logger.error("received badly formatted text")
    Ok
  }
  def newHunt = Action{request=>
    request.body.asJson match {
      case Some(json) => Json.fromJson[Hunt](json) match {
        case JsSuccess(hunt,_) => {
          try {
            Hunt.insert(hunt) match {
              case Some(id) => Hunt.findOneById(id) match {
                case Some(dbhunt) => Ok(Json.toJson(dbhunt))
                case None => InternalServerError("could not retrieve hunt object just inserted")
              }
              case None => InternalServerError("error occured on insert")
            }

          }catch{
            case e:SalatInsertError => InternalServerError("error occured on insert")
          }
        }
        case JsError(errors) => BadRequest("couldn't parse json")
      }
      case None => BadRequest("no json to create hunt")
    }
  }
  def getHunt(hunt: String) = Action{ request=>
    Hunt.findOneById(new ObjectId(hunt)) match {
      case Some(hunt) => Ok(Json.toJson(hunt))
      case None => NotFound("hunt not found")
    }
  }
  def deleteHunt(hunt:String) = Action{ request =>
    val wr = Hunt.removeById(new ObjectId(hunt))
    if(wr.getLastError.ok()) Ok
    else InternalServerError
  }
  private def encodeURIComponent(s:String):String = {
    URLEncoder.encode(s, "UTF-8")
      .replaceAll("\\+", "%20")
      .replaceAll("\\%21", "!")
      .replaceAll("\\%27", "'")
      .replaceAll("\\%28", "(")
      .replaceAll("\\%29", ")")
      .replaceAll("\\%7E", "~");
  }
  def sendText(to:String) = Action {request=>
    val base_url = ConfigFactory.load().getString("NEXMO_BASE_URL")
    val from = ConfigFactory.load().getString("NEXMO_NUMBER")
    val apiKey = ConfigFactory.load().getString("NEXMO_API_KEY")
    val apiSecret = ConfigFactory.load().getString("NEXMO_API_SECRET")
    Group.findOneById(new ObjectId(to)) match {
      case Some(group) => request.body.asJson match {
        case Some(json) => {
          (json \ "message").asOpt[String] match {
            case Some(message) => {
              val request:Req = url(s"$base_url?" +
                s"api_key=$apiKey&api_secret=$apiSecret&from=$from&to=${group.endpoint}&text=${encodeURIComponent(message)}")
              val response:Response = Await.result(Http(request),10.seconds)
              Logger.info("Nexmo response: "+response.getResponseBody)
              Group.addMessage(group.id,GroupMessage(message,Direction.Received))
              Ok
            }
            case None => BadRequest("no message property found in json")
          }
        }
        case None => BadRequest("no message received")
      }
      case None => {
        Logger.error("ScavengerHunt.sendText: no group found given id")
        BadRequest("ScavengerHunt.sendText: no group found given id")
      }
    }
  }
  def uploadItem = Action{request =>
    request.body.asJson match {
      case Some(json) => Json.fromJson[Item](json) match {
        case JsSuccess(item,_) => Item.insert(item) match {
          case Some(id) => Item.findOneById(id) match {
            case Some(dbitem) => Ok(Json.toJson(dbitem))
            case None => InternalServerError("couldn't find item just inserted")
          }
          case None => InternalServerError("error occured while inserting item")
        }
        case JsError(errors) => BadRequest(JsError.toFlatJson(errors))
      }
      case None => BadRequest("no json found")
    }
  }
  def getItems(huntId: String) = Action{request =>
    Ok
  }
  def getItem(huntId: String, clueId: String) = Action{request =>
    Ok
  }
  def getAllItems = Action{request =>
    Ok(Json.toJson(Item.findAll().foldLeft[Seq[Item]](Seq())((acc,item) => acc :+ item)))
  }
}
