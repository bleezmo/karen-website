package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import com.typesafe.plugin._

object Application extends Controller {

  def index = Assets.at("/public","index.html");
  def postMail = Action(parse.urlFormEncoded) { request =>
    request.body.get("name") match {
      case Some(name :: _) => request.body.get("email") match {
        case Some(email :: _) => request.body.get("message") match {
          case Some(message :: _) => {
            sendMail(name,email,message)
            Ok
          }
          case _ => BadRequest("message missing")
        }
        case _ => BadRequest("email missing")
      }
      case _ => BadRequest("name missing")
    }
  }

  private def sendMail(name:String,email:String,message:String) = {
    val mail = use[MailerPlugin].email
    mail.setSubject("karencecilia.com message from "+name)
    mail.addRecipient("infokarencecilia@gmail.com")
    mail.addFrom(email)
    mail.send(name+" has sent you a message:\n\n"+message)
  }
  
}