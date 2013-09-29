package controllers

import play.api.mvc.{Action, Controller}
import models.Hunt
import org.bson.types.ObjectId
import play.api.libs.json._
import com.novus.salat.dao.SalatInsertError

object ScavengerHunt extends Controller{
  def index = Action{request=>
    Ok(views.html.scavengerhunt())
  }
  def receiveText = Action {request =>
    Ok
  }
  def getHunts = Action{request=>
    Ok
  }
  def newHunt = Action{request=>
    request.body.asJson match {
      case Some(json) => Json.fromJson[Hunt](json) match {
        case JsSuccess(hunt,_) => {
          try {
            Hunt.insert(hunt,Hunt.defaultWriteConcern) match {
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
  def getHunt(hunt: ObjectId) = Action{request=>
    Ok
  }
}
