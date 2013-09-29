import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "karen-website"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2",
      "se.radley" %% "play-plugins-salat" % "1.3.0"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      routesImport += "se.radley.plugin.salat.Binders._",
      templatesImport += "org.bson.types.ObjectId"
    )

}
