package model

import anorm._
import java.sql.Timestamp
import java.util.Locale
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.db.DB
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import securesocial.core.{IdentityId, AuthenticationMethod, OAuth1Info, OAuth2Info, PasswordInfo, Identity, SecureSocial}
import play.api.mvc.RequestHeader
import securesocial.core.providers.utils.PasswordHasher
import play.api.Logger

object Users extends Table[User]("user") {
  def email                   = column[Option[String]]("email_address") // must be unique and must be provided, despite what securesocial thinks
  protected def userId        = column[String]("userid") // must be unique
  def firstName               = column[String]("first_name")
  def lastName                = column[String]("last_name")
  protected def password      = column[String]("password")
  protected def avatarUrl     = column[Option[String]]("avatarurl")
  protected def provider      = column[Option[String]]("provider")
  def id                      = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def * = email ~ userId ~ firstName ~ lastName ~ password ~ avatarUrl ~ provider ~ id.? <> (User, User unapply _)

  protected def autoInc = email ~ userId ~ firstName ~ lastName ~ password ~ avatarUrl ~ provider ~ returning id

  def add(newUser: User): User = Global.database.withSession { implicit session: Session =>
    if (Users.findByEmail(newUser.email).isEmpty) {
      val index = autoInc.insert((newUser.email, newUser.userId, newUser.firstName, newUser.lastName, newUser.password,
        newUser.avatarUrl, newUser.provider))
      val inserted = newUser.copy(id=Some(index))
      inserted
    } else {
      throw new Exception(s"User with email ${newUser.email.getOrElse("")} already exists")
    }
  }

  def emailAddresses = foreignKey("ADDR_FK", email, CustomerAddresses)(_.email)

  def findByCurrentUser(implicit header: RequestHeader): Option[User] = for {
    socialUser <- SecureSocial.currentUser
    user <- findByEmail(socialUser.email)
  } yield user

  def findByEmail(email: Option[String]): Option[User] = Global.database.withSession { implicit session: Session =>
    queryAll.where(_.email === email).firstOption
  }

  override def findById(id: Option[Long]) = {
    val x = super.findById(id)
    x
  }

  def findByUserId(userId: String): Option[User] = Global.database.withSession { implicit session: Session =>
    val result = queryAll.where(_.userId === userId).firstOption
    result
  }

  def queryAll = Query(Users)

  protected def queryId(id: Long) = queryAll.where(_.id === id)

  /** Ensure that autoInc value is properly set when the app starts */
  def setAutoInc() = DB.withConnection { implicit connection =>
    SQL("""SELECT setval('user_id_seq', (SELECT max(id) FROM public.user));""").execute()
  }

  def isDuplicateEmail(aUser: User): Boolean = {
    val maybeUser: Option[User] = Users.findByEmail(aUser.email)
    val isSameEmail: Boolean = maybeUser.exists(_.email==aUser.email)
    val isNewDup = aUser.id.isEmpty && isSameEmail
    val isOldDup = maybeUser.exists(_.id != aUser.id && isSameEmail)
    isNewDup || isOldDup
  }
}

case class User(
  email: Option[String],

  /** Login id */
  userId: String,
  firstName: String,
  lastName: String,
  password: String,

  /** Automatically filled in by SecureSocial, not by the user */
  avatarUrl: Option[String] = None,

  /** Added for SecureSocial. Won't work for SSO */
  provider: Option[String] = Some("userpass"),
  override val id: Option[Long] = None

) extends HasId with Identity {

  val dateTimeFmtLong = DateTimeFormat.longDateTime.withLocale(locale)

  val dateTimeFmtShort = DateTimeFormat.shortDateTime.withLocale(locale)

  val dateFmtLong = DateTimeFormat.longDate.withLocale(locale)

  val dateFmtShort = DateTimeFormat.shortDate.withLocale(locale)

  val timeFmtLong = DateTimeFormat.longTime.withLocale(locale)

  val timeFmtShort = DateTimeFormat.shortTime.withLocale(locale)

  def authMethod: AuthenticationMethod = ???

  def oAuth1Info: Option[OAuth1Info] = ???

  def oAuth2Info: Option[OAuth2Info] = ???

  def passwordInfo: Option[PasswordInfo] = Some(PasswordInfo(PasswordHasher.BCryptHasher, password))

  def identityId: IdentityId = IdentityId(userId, provider.getOrElse(""))
}
