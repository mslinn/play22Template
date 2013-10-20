package controllers

import model.training._
import org.specs2.mutable._
import play.api.db.DB
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._
import scala.slick.driver.PostgresDriver.simple._
import org.slf4j.LoggerFactory

class SectionTest extends Specification {
  val Logger = LoggerFactory.getLogger("test")

  def database = Database.forDataSource(DB.getDataSource("test"))

  def postgresDatabase(name: String = "default", options: Map[String, String] = Map.empty): Map[String, String] =
    Map(
      "db.test.driver"   -> "org.postgresql.Driver",
      "db.test.user"     -> "postgres",
      "db.test.password" -> "hithere",
      "db.test.url"      -> "jdbc:postgresql://localhost/slinnbooks"
    )

  object x {
    val groupId = -1234L
    val courseId = -1234L
    val sectionId = -1234L
    val course: Course = Courses.upsert(Course(Some(groupId), "course_testCourse123", id=Some(-1234L)))
    val lectures =  List(
      Lectures.upsert(Lecture(Some(sectionId), "Test lecture description 1", "lecture_testSku1", "Transcript 1", "Lecture Title 1", id=Some(-1234L))),
      Lectures.upsert(Lecture(Some(sectionId), "Test lecture description 2", "lecture_testSku2", "Transcript 2", "Lecture Title 2", id=Some(-1235L))),
      Lectures.upsert(Lecture(Some(sectionId), "Test lecture description 3", "lecture_testSku3", "Transcript 3", "Lecture Title 3", id=Some(-1236L))),
      Lectures.upsert(Lecture(Some(sectionId), "Test lecture description 4", "lecture_testSku4", "Transcript 4", "Lecture Title 4", id=Some(-1237L)))
    )

    var lectureIdStr: String = lectures.map(_.id).mkString(",")
    var section: Section = Section(Some(courseId), "section_testName1", "Test Section Title 1", Some(lectureIdStr), id=Some(sectionId))
  }

  def fakeApp[T](block: => T): T =
    running(FakeApplication(additionalConfiguration = postgresDatabase("test") ++ Map("evolutionplugin" -> "disabled"))) {
      database.withSession { implicit s: Session => block }
    }

  "LectureIdList" should {
    "be in the proper format" in fakeApp {
      x.lectureIdStr.split(",").size must equalTo(4)
    }
  }

  "Lectures" should {
    "associate with their section" in fakeApp {
      val sectionName1 = x.lectures(1).sectionName
      sectionName1 must equalTo("testName")
    }
  }
}
