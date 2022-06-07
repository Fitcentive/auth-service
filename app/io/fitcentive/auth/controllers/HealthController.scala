package io.fitcentive.auth.controllers

import play.api.mvc._

import javax.inject._

@Singleton
class HealthController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def healthCheck: Action[AnyContent] = Action { Ok("Server is alive!") }

}
