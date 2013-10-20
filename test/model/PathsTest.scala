package model

import model.training._
import play.api.Play.current
import org.slf4j.LoggerFactory
import org.specs2.mutable._

/* To run:
 * `bin/play 'test-only model.PathsTest'` */
class PathsTest extends Specification {
  val Logger = LoggerFactory.getLogger("test")

  "Paths" should {
    "work" in {
      success
    }
  }
}
