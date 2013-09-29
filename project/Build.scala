import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "karen-website"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2",
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
      "com.novus" %% "salat" % "1.9.2"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(

    )

}
