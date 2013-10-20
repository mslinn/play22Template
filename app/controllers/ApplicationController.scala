package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import securesocial.core.SecureSocial
import views.{html => viewz}
import views.html.helper._

object ApplicationController extends Controller with SecureSocial {

  def index = UserAwareAction { implicit requestWithUser =>
    Ok(views.html.index("Play 2.2 Template"))
  }
}
