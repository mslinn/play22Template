package model

import org.slf4j.LoggerFactory
import org.specs2.mutable._
import play.api.test.Helpers._
import play.api.test.FakeApplication

/* To run:
 * `bin/play 'test-only model.PersistenceTest'` */
class PersistenceTest extends Specification {
  val Logger = LoggerFactory.getLogger("test")

  "PersistenceTest" should {
    "work" in {
      running(FakeApplication()) {
        Context.pubConf.id shouldEqual Some(1)
      }
    }
  }
}
