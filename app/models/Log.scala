package models

import play.api.Logger

object Log {
  def e(msg:String) = {
    Logger.error("SH_ERROR: "+msg)
  }
}
