package model

import collection.JavaConverters._
import com.typesafe.config.ConfigFactory
import java.io.File
import language.postfixOps
import model._
import model.training._
import model.officeHours._
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import play.api.cache._
import service.officeHours._

/** Only lazy vals and defs are allowed, no vals or any other code blocks, otherwise delayedInit() gets invoked twice
  * which means around() gets invoked twice */
object Context {
  lazy val Logger = LoggerFactory.getLogger("test")

  lazy val dotAws = System.getProperty("user.home") + "/.aws"

  private lazy val confStr = "aws = " + io.Source.fromFile(dotAws).mkString
  assert(new File(Context.dotAws).exists)

  private lazy val config = ConfigFactory.parseString(confStr)
  private lazy val map = config.getList("aws").asScala.head.unwrapped.asInstanceOf[java.util.HashMap[String, String]]
  if (Logger.isDebugEnabled)
    config.entrySet.asScala.map(_.getKey) foreach { x => Logger.debug(x) }

  lazy val accessKey = map.get("accessKey")
  assert(accessKey nonEmpty)
  Logger.debug(s"accessKey=$accessKey")

  lazy val secretKey = map.get("secretKey")
  assert(secretKey nonEmpty)
  Logger.debug(s"secretKey=$secretKey")

  lazy val now = new java.sql.Date(System.currentTimeMillis)
  lazy val tenYearsHence = now.getTime + 1000*60*60*24*365*10

  clear()

  lazy val instructor1 = maybeCreateUser(User(
    Some("instructor@mslinn.com"),
    userId="mslinn",
    firstName="Mike",
    lastName="Slinn",
    password="mslinn",
    avatarUrl=Some("http://2.gravatar.com/avatar/dd80aab570fdb5f37e62de5422f44ed4"),
    enabled=true,
    id=Some(1)))

  lazy val ta1 = maybeCreateUser(User(
    Some("ta@micronauticsresearch.com"),
    userId="ta1",
    firstName="Fred",
    lastName="Flintstone",
    password="ta1",
    avatarUrl=Some("http://userserve-ak.last.fm/serve/_/33397865/Fred+Flintstone+FredFlintstone.jpg"),
    enabled=true,
    id=Some(2)))

  lazy val student1 = maybeCreateUser(User(
    Some("student@scalacourses.com"),
    userId="student1",
    firstName="Tasha",
    lastName="Yar",
    password="student1",
    avatarUrl=Some("http://i150.photobucket.com/albums/s112/ajlobster/Where%20No%20One%20Has%20Gone%20Before/Screenshot2011-02-13at125841PM1.png"),
    enabled=true,
    id=Some(3)))

  lazy val student2 = maybeCreateUser(User(
    Some("student2@mslinn.com"),
    userId="student2",
    firstName="BamBam",
    lastName="Rubble",
    password="student2",
    avatarUrl=Some("http://www.scalacoursestest.com.s3.amazonaws.com/1/assets/images/BammBammRubble.png"),
    enabled=true,
    id=Some(4)))

  lazy val student3 = maybeCreateUser(User(
    Some("student3@mslinn.com"),
    userId="student3",
    firstName="Pebbles",
    lastName="Flintstone",
    password="student3",
    avatarUrl=Some("http://www.scalacoursestest.com.s3.amazonaws.com/1/assets/images/PebblesFlintstone.png"),
    enabled=true,
    id=Some(5)))

  lazy val privUser1 = PrivilegedUsers.upsert(PrivilegedUser(
    awsSecretKey=secretKey,
    awsAccessKeyId=accessKey,
    confirmed=now,
    userId=instructor1.id.get,
    bio="This is the biography",
    privateNotes=Some("This is a private note"),
    confirmedByIdOption=None,
    active=true,
    id=instructor1.id))

  lazy val pubConf: PublisherConfig = {
    Logger.debug(s"Model.sitePubConfigId=${Model.sitePubConfigId}")
    PublisherConfigs.save(PublisherConfig(
      "testPubConfig",
      "awsAccount",
      secretKey,
      accessKey,
      bucketName="www.mocctest.com",
      created=now,
      //packedVideoParams, // use default value
      transcript=Some("This is the pubConf transcript"),
      image=Some("http://upload.wikimedia.org/wikipedia/en/2/26/Classic_bugsbunny.png"),
      emailLogo=Some("http://upload.wikimedia.org/wikipedia/en/2/26/Classic_bugsbunny.png"),
      video=Some("https://s3.amazonaws.com/www.scalacoursestest.com/1/assets/videos/pubconf_scalaCourses.mp4"),
      trialUntil=Some(new java.sql.Date(tenYearsHence)),
      paidUntil=Some(new java.sql.Date(tenYearsHence)),
      groups=None,
      comments=Some("This is the comment"),
      active=true,
      receiverEmail=Some("mslinn@micronauticsresearch.com"),
      id=Some(Model.sitePubConfigId)))
  }

  lazy val group1 = Groups.add(Group(
    sku="group1",
    pubConfId=pubConf.id.get,
    title="Test Group 1",
    transcript=Some("Group transcript 1"),
    image=Some("http://upload.wikimedia.org/wikipedia/en/2/26/Classic_bugsbunny.png"),
    video=Some("https://s3.amazonaws.com/www.scalacoursestest.com/1/html/Cadenza/assets/videos/group_CourseCreator.mp4"),
    courses=None,
    active=true
  ))

  lazy val course1 = Courses.add(Course(
    group1.id,
    "course1",
    shortDescription="Short course description 1",
    transcript="Course 1 transcript",
    title="Course 1 title",
    price=Some(100),
    video=Some("https://s3.amazonaws.com/www.scalacoursestest.com/1/html/Cadenza/assets/videos/course_CourseCreator101.mp4"),
    //privacy="", // use default
    //language,   // not implemented
    projectHome=None,
    repository=None,
    instructorId=instructor1.id,
    //keywords, // use default
    //category, // not implemented
    //goals,    // not implemented
    //audience, // not implemented
    //instructionalLevel, // use default
    image=Some("http://upload.wikimedia.org/wikipedia/en/2/26/Classic_bugsbunny.png"),
    //studentTasks,
    sections=None,
    active=true,
    paypalButtonId=Some("fakePaypalButton")
  ))

  lazy val section1 = Sections.add(Section(
    courseId=course1.id,
    name="section_1",
    title="Section 1 title",
    video=None,
    description=Some("Section 1 description"),
    lectures=None,
    active=true
  ))

  lazy val lecture1 = Lectures.add(Lecture(
    parentId=section1.id,
    description="",
    sku="lecture_sku1",
    transcript="Lecture 1 transcript",
    title="Lecture 1 title",
    price=Some(123),
    video=Some("https://s3.amazonaws.com/www.scalacoursestest.com/1/html/Cadenza/assets/videos/lecture_htmlEditorSpellcheck.mp4"),
    promotional=true,
    active=true,
    optional=false
  ))

  lazy val roleInstructor1 = Roles.add(Role(instructor1.userId, RoleEnum.Instructor.toString,        Some(course1.sku)))
  lazy val roleTA1         = Roles.add(Role(ta1.userId, RoleEnum.TeachingAssistant.toString, Some(course1.sku)))
  lazy val roleStudent1    = Roles.add(Role(student1.userId, RoleEnum.Student.toString,           Some(course1.sku)))
  lazy val roleStudent2    = Roles.add(Role(student2.userId, RoleEnum.Student.toString,           Some(course1.sku)))
  lazy val roleStudent3    = Roles.add(Role(student3.userId, RoleEnum.Student.toString,           Some(course1.sku)))

  def createOHData(lecture: Lecture) {
    val inquiry1 = Inquiries.save(Inquiry("One", "", student1.id.get, lecture.id.get))
    val inquiry2 = Inquiries.save(Inquiry("Two", "", student2.id.get, lecture.id.get))
    val inquiry3 = Inquiries.save(Inquiry("Three", "", student3.id.get, lecture.id.get))
    for {
      lectureId <- lecture.id
      course <- lecture.course
      courseId <- course.id
      inquiry1Id <- inquiry1.id
      inquiry2Id <- inquiry2.id
      inquiry3Id <- inquiry3.id
      student1Id <- student1.id
      student2Id <- student2.id
      student3Id <- student3.id
      ta1Id <- ta1.id
      instructor1Id <- instructor1.id
    } {
      val q1 = Questions.save(Question("original", "Why do we need oxygen?", "Clean air is getting hard to find",
        courseId, lectureId, None, inquiry1Id, student1Id))
      val q2 = Questions.save(Question("followon", "Is there such as thing as too much oxygen?", "I hang out in O2 bars every night",
        courseId, lectureId, q1.id, inquiry1Id, student2Id))
      val q3 = Questions.save(Question("followon", "What happens if we do not get enough oxygen?", "My O2 bar bill is getting pretty high",
        courseId, lectureId, q2.id, inquiry1Id, student3Id))

      val q4 = Questions.save(Question("original", "Why can't we breathe CO2, like plants?", "Plants have secret lives",
        courseId, lectureId, None, inquiry2Id, student1Id))
      val q5 = Questions.save(Question("followon", "Why are these questions so silly?", "I am searching fro teh ting",
        courseId, lectureId, q4.id, inquiry2Id, student2Id))
      val q6 = Questions.save(Question("followon", "What if we eat lots of chlorophyll?", "It isn't easy being green",
        courseId, lectureId, q5.id, inquiry2Id, student3Id))

      val q7 = Questions.save(Question("original", "Can't we just implant rebreathers?", "We'd look really scary",
        courseId, lectureId, None, inquiry2Id, student1Id))
      val q8 = Questions.save(Question("followon", "What if we genetically engineered our skin to include chloroplasts?",
        "There would be little green men on earth", courseId, lectureId, q7.id, inquiry2Id, student3Id))
      val q9 = Questions.save(Question("followon", "Do you think green skin would be considered sexy?", "Ooh, baby!",
        courseId, lectureId, q7.id, inquiry2Id, student2Id))
      val q10 = Questions.save(Question("followon", "Would short people be called 'little green men'?", "Of course!",
        courseId, lectureId, q7.id, inquiry2Id, student3Id))

      val start1 = new DateTime(2013, 7, 26, 12, 0, 0, 0)
      val ohSlot1 = OfficeHoursSlots.upsert(OfficeHoursSlot(ta1Id, courseId, start1, start1.plus(Period.minutes(50)), None, inquiryId=inquiry1.id))
      val start2 = start1.plus(Period.days(1))
      val ohSlot2 = OfficeHoursSlots.upsert(OfficeHoursSlot(ta1Id, courseId, start2, start2.plus(Period.minutes(50)), None, inquiryId=inquiry2.id))
      val start3 = start2.plus(Period.days(1))
      val ohSlot3 = OfficeHoursSlots.upsert(OfficeHoursSlot(ta1Id, courseId, start3, start3.plus(Period.minutes(50)), None, inquiryId=inquiry3.id))

      val recording1 = Recordings.upsert(Recording("officeHours_1", ohSlot1.id.get, "", ""))
      val recording2 = Recordings.upsert(Recording("officeHours_2", ohSlot2.id.get, "", ""))
      val recording3 = Recordings.upsert(Recording("officeHours_3", ohSlot3.id.get, "", ""))

      val fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")
      val startTime = fmt.parseDateTime("2013/05/01 14:00")
      val endTime = fmt.parseDateTime("2013/05/01 14:50")

      val ssr = StudentSlotRequests.save(StudentSlotRequest(
        inquiryId=inquiry1.id,
        userId=student1Id,
        courseId=courseId,
        startTime=startTime,
        endTime=endTime,
        constraint=SlotConstraintsEnum.RelatedQuestions.toString,
        whenConfirmed=Some(DateTime.now),
        taIdAssigned=ta1.id))

      val tar = OfficeHoursSlots.save(OfficeHoursSlot(
        expertId=ta1Id,
        courseId=courseId,
        startTime=startTime,
        endTime=endTime,
        payPalButtonId=None,
        inquiryId=inquiry1.id,
        whenConfirmed=Some(new DateTime(now.getTime)),
        meetingJson=None))
    }
  }

  /** Empty the database */
  def clear(): Unit = {
    import scala.slick.driver.PostgresDriver.simple._

    Global.database.withSession { implicit session: Session =>
      Questions.queryAll.delete
      Inquiries.queryAll.delete
      Recordings.queryAll.delete
    }
    Questions.setAutoInc()
    Inquiries.setAutoInc()
    Recordings.setAutoInc()
    OfficeHoursSlots.setAutoInc()

    Recordings.findAll.foreach { recording => Recordings.deleteById(recording.id) }
    Votes.findAll.foreach { vote => Votes.deleteById(vote.id) }
    Roles.findAll.map(_.delete())
    Lectures.findAll.map(_.deleteCascade())
    Sections.findAll.map(_.deleteCascade())
    Courses.findAll.map(_.deleteCascade())
    Groups.findAll.map(_.deleteCascade())
    PublisherConfigs.findAll.map(_.deleteCascade())
    PrivilegedUsers.findAll.foreach { user => PrivilegedUsers.deleteById(user.id) }
    Users.findAll.foreach { user =>
      Logger.debug(s"Deleting user ${user.fullName} (${user.email.getOrElse("")}})")
      Users.deleteById(user.id)
    }

    TrainingPersistence.flushCache()
    Logger.info("Database and cache should be empty now")
  }

  /** Unit testing Play apps causes this method to be called multiple times */
  def maybeCreateUser(user: User): User = {
    if (Users.findByEmail(user.email).isDefined)
      user
    else
      Users.upsert(hashUser(user))
  }
}
