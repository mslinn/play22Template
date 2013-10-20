package controllers.scanner

import model.training._
import org.slf4j.LoggerFactory
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ScannerTest extends Specification {
  val Logger = LoggerFactory.getLogger("test")
  val scanner = new FileScanner(cdnImportMedia, cdnIgnores)
  val groupName = "scalaJavaInterop"
  val courseName = "ooCompat"
  val sectionName = "section_JavaFromScala"
  val lectureName = "lectureJavaVisibility"

  "FileScanner" should {
    "fail to find a non-existant group of courses" in {
      val groupFile = scanner.groupFile("asdf")
      groupFile === None
    }

    "find a group of courses" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      groupFile !== None
      groupFile.getAbsolutePath must endWith("scalaJavaInterop")
    }

    "find all course groups" in {
      val groupFiles = scanner.groupFiles
      groupFiles.size must be >= 1
    }

    "find a course in a group" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      val Some(courseFile) = scanner.courseFile(groupFile, courseName)
      courseFile !== None
      courseFile.getAbsolutePath must endWith(s"$groupName/html/$courseName")
    }

    "find all courses in a group" in {
      val Some(groupFile)  = scanner.groupFile(groupName)
      val courseFiles = scanner.courseFiles(groupFile)
      courseFiles.size must be >= 1
    }

    "find a section in a course" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      val Some(courseFile) = scanner.courseFile(groupFile, courseName)
      val sectionFile = scanner.sectionFile(courseFile, sectionName)
      sectionFile !== None
      sectionFile.get.getAbsolutePath must endWith(s"$groupName/html/$courseName/$sectionName")
    }

    "find all lectures in a section of a course" in {
      val Some(groupFile)  = scanner.groupFile(groupName)
      val Some(courseFile) = scanner.courseFile(groupFile, courseName)
      val sectionFiles = scanner.sectionFilesInCourse(courseFile)
      sectionFiles.size must be >= 1
    }

    "find a lecture in a course" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      val Some(courseFile) = scanner.courseFile(groupFile, courseName)
      val Some(sectionFile) = scanner.sectionFile(courseFile, sectionName)
      val Some(lectureFile) = scanner.lectureFileInSection(sectionFile, lectureName)
      lectureFile !== None
      lectureFile.getAbsolutePath must endWith(s"$groupName/html/$courseName/$sectionName/$lectureName.html")
    }

    "find all lectures in a section" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      val Some(courseFile) = scanner.courseFile(groupFile, courseName)
      val Some(sectionFile) = scanner.sectionFile(courseFile, sectionName)
      val lectureFiles = scanner.lectureFilesInSection(sectionFile)
      lectureFiles.size must be >= 1
    }

    "find all lectures in a course" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      val Some(courseFile) = scanner.courseFile(groupFile, courseName)
      val lectureFiles = scanner.lectureFilesInCourse(courseFile)
      lectureFiles.size must be >= 1
    }

    "find all lectures in a course group" in {
      val Some(groupFile) = scanner.groupFile(groupName)
      val lectureFiles = scanner.lectureFilesInGroup(groupFile)
      lectureFiles.size must be >= 1
    }

    "find all lectures in all groups" in {
      val lectureFiles = scanner.lectureFilesInGroups
      lectureFiles.size must be >= 1
    }
  }
}
