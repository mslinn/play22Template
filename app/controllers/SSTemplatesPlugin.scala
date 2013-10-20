package controllers

import play.api.data.Form
import play.api.templates.{Txt, Html}
import securesocial.controllers.TemplatesPlugin
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.core.{Identity, SecuredRequest, SocialUser}
import play.api.mvc.{RequestHeader, Request}

class SSTemplatesPlugin(application: play.api.Application) extends TemplatesPlugin {

  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String]=None): Html =
    views.html.secureSocial.login(form, msg)(request.flash, request, request.acceptLanguages.head)

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html =
    views.html.secureSocial.Registration.signUp(form, token)(request.flash, request, request.acceptLanguages.head)

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html =
    views.html.secureSocial.Registration.startSignUp(form)(request.flash, request, request.acceptLanguages.head)

  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html =
    views.html.secureSocial.Registration.startResetPassword(form)(request.flash, request, request.acceptLanguages.head)

  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html =
    views.html.secureSocial.Registration.resetPasswordPage(form, token)(request.flash, request, request.acceptLanguages.head)

  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html =
    views.html.secureSocial.passwordChange(form)(request.flash, request, request.acceptLanguages.head)

  /** @return the email sent when a user starts the sign up process */
  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) =
    (None, Some(securesocial.views.html.mails.signUpEmail(token)))

  def getAlreadyRegisteredEmail(user: SocialUser)(implicit request: RequestHeader): String =
    securesocial.views.html.mails.alreadyRegisteredEmail(user).body

  def getWelcomeEmail(user: SocialUser)(implicit request: RequestHeader): String =
    securesocial.views.html.mails.welcomeEmail(user).body

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) =
    (None, Some(securesocial.views.html.mails.unknownEmailNotice(request)))

  def getSendPasswordResetEmail(user: SocialUser, token: String)(implicit request: RequestHeader): String =
    securesocial.views.html.mails.passwordResetEmail(user, token).body

  def getPasswordChangedNoticeEmail(user: SocialUser)(implicit request: RequestHeader): String =
    securesocial.views.html.mails.passwordChangedNotice(user).body

  // todo brand this
  def getNotAuthorizedPage[A](implicit request: Request[A]): Html =
    securesocial.views.html.notAuthorized()

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) =
    (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) =
    (None, Some(securesocial.views.html.mails.welcomeEmail(user)))

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) =
    (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) =
    (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
}
