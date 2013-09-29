import play.api._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    Logger.info(app.plugins.map(_.toString).toString())
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
