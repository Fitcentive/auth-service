package infrastructure.actions

import domain.{AuthorizedUser, UserRequest}
import play.api.http.HeaderNames
import play.api.mvc.{ActionBuilder, AnyContent, BodyParser, BodyParsers, Request, Result, Results, WrappedRequest}
import services.{SettingsService, TokenValidationService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthAction @Inject() (bodyParser: BodyParsers.Default, tokenValidationService: TokenValidationService)(implicit
  ec: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  private val headerTokenRegex = """Bearer (.+?)""".r

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
    extractBearerToken(request) map { token =>
      // todo - validation fails here because SSO token does not have user_id field
      tokenValidationService.validateJwt[AuthorizedUser](token) match {
        case Right(authorizedUser) => block(UserRequest(authorizedUser, request))
        case Left(error)           => Future.successful(Results.Unauthorized(error.reason))
      }
    } getOrElse Future.successful(Results.Unauthorized) // no token was sent - return 401

  // Helper for extracting the token value
  private def extractBearerToken[A](request: Request[A]): Option[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => token
    }
}
