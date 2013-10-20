package service

import java.sql.Timestamp
import play.api.Application
import securesocial.core._
import model.{Tokens, User, Users}
import securesocial.core.PasswordInfo
import securesocial.core.providers.Token
import securesocial.core.providers.utils.PasswordHasher

class SSUserService(application: Application) extends UserServicePlugin(application) {

  /** Assumes that the user always uses the same authentication provider; will need to make this more complex in order
    * to support SSO */
  def find(userId: IdentityId): Option[SocialUser] = {
      val users = Users.findByUserId(userId.userId).map { user => // TODO is userId.userId right?
        SocialUser(
          identityId = IdentityId(user.userId, user.provider.getOrElse("")),
          firstName = user.firstName,
          lastName = user.lastName,
          fullName = user.fullName,
          email = user.email,
          avatarUrl = user.avatarUrl,
          authMethod = AuthenticationMethod.UserPassword,
          oAuth1Info = None,
          oAuth2Info = None,
          passwordInfo = Some(PasswordInfo(PasswordHasher.BCryptHasher, user.password))
        )}.toList
      users.headOption
    }

  /** @param providerId is ignored */
  def findByEmailAndProvider(email: String, providerId: String): Option[SocialUser] = {
    val userCandidates = Users.findByEmail(Some(email))//.filter(_.provider==providerId)
    val users = userCandidates.map(user =>
      SocialUser(
        identityId = IdentityId(user.userId, user.provider.getOrElse("")),
        firstName = user.firstName,
        lastName = user.lastName,
        fullName = user.fullName,
        email = user.email,
        avatarUrl = user.avatarUrl,
        authMethod = AuthenticationMethod.UserPassword,
        oAuth1Info = None,
        oAuth2Info = None,
        passwordInfo = Some(PasswordInfo(PasswordHasher.BCryptHasher, user.password))
      )).toList
    users.headOption
  }

  /** Should have been called upsert; actually, it only makes sense to update the avatar and the last logged in timestamp */
  def save(identity: Identity): Identity = {
    val now = new Timestamp(System.currentTimeMillis)

    def createUser(ident: Identity): User =
      User(ident.email, ident.identityId.userId, ident.firstName, ident.lastName,
           ident.passwordInfo.map(_.password).getOrElse(""), ident.avatarUrl, Some(ident.identityId.providerId),
           authenticated=Some(now), lastLoggedIn=Some(now))

    val candidates = Users.findByUserId(identity.identityId.userId)
    val socialUsers = candidates.map(user =>
      SocialUser(
        identityId = IdentityId(user.userId, user.provider.getOrElse("")),
        firstName = user.firstName,
        lastName = user.lastName,
        fullName = user.fullName,
        email = user.email,
        avatarUrl = user.avatarUrl,
        authMethod = AuthenticationMethod.UserPassword,
        oAuth1Info = None,
        oAuth2Info = None,
        passwordInfo = Some(PasswordInfo(PasswordHasher.BCryptHasher, identity.passwordInfo.map(_.password).getOrElse("")))
      )).toList
    socialUsers match {
      case sUser :: tail =>
        Users.findByEmail(sUser.email).map { user =>
          val modifiedUser = user.copy(avatarUrl    = sUser.avatarUrl,
                                       password     = sUser.passwordInfo.map(_.password).getOrElse(""),
                                       lastLoggedIn = Some(now))
          Users.upsert(modifiedUser)
        }.getOrElse{
          val newUser = createUser(identity)
          Users.upsert(newUser)
        }

      case _ =>
        val newUser = createUser(identity)
        Users.upsert(newUser)
    }
  }

  def save(token: Token): Unit = Tokens.save(token)

  def findToken(uuid: String): Option[Token] = Tokens.findByUuid(uuid)

  def deleteToken(uuid: String): Unit = Tokens.deleteByUuid(uuid)

  def deleteTokens(): Unit = Tokens.deleteAll()

  def deleteExpiredTokens(): Unit = Tokens.deleteExpired()
}
