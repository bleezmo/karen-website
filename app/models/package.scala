package models

import com.novus.salat.dao._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.novus.salat.{TypeHintFrequency, StringTypeHintStrategy, Context}
import play.api.{Logger, Play}
import play.api.Play.current
import com.typesafe.config.ConfigFactory

package object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    context
  }
  private var _db:Option[MongoDB] = None
  def db:MongoDB = {
    _db.getOrElse {
      val uri = MongoClientURI(ConfigFactory.load().getString("mongo.default.uri"))
      Logger.info("connecting: "+uri.toString())
      val client = MongoClient(uri)
      client(uri.database.get)
    }
  }
}