package controllers

import play.api.mvc.{Action, Controller}
import models.{Group, Hunt}
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

object ScavengerHunt extends Controller{
  def index = Action{request=>
    Logger.info(request.uri)
    Logger.info(request.getQueryString("blergl").toString)
    Ok(views.html.scavengerhunt())
  }
  def receiveText = Action {request =>
    Ok
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
    if (number.isDefined && message.isDefined){
      message.get match {
        case x if x.startsWith("start") => {

        }
        case x =>
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
  def getHunt(hunt: String) = Action{request=>
    Hunt.findOneById(new ObjectId(hunt)) match {
      case Some(hunt) => Ok(Json.toJson(hunt))
      case None => NotFound("hunt not found")
    }
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
    request.body.asJson match {
      case Some(json) => {
        (json \ "message").asOpt[String] match {
          case Some(message) => {
            val request:Req = url(s"$base_url?" +
              s"api_key=$apiKey&api_secret=$apiSecret&from=$from&to=1$to&text=${encodeURIComponent(message)}")
            val response:Response = Await.result(Http(request),10.seconds)
            Logger.info(response.getResponseBody)
            Ok
          }
          case None => BadRequest("no message property found in json")
        }
      }
      case None => BadRequest("no message received")
    }
  }
  def uploadClue(huntId: String) = Action{request =>
  Ok
  }
  def getClues(huntId: String) = Action{request =>
    Ok
  }
  def getClue(huntId: String, clueId: String) = Action{request =>
    Ok
  }
}
