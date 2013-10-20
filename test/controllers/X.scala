package controllers

import play.api.test._
import play.api.test.Helpers._
import org.specs2.mutable._

class X extends Specification {
  "X" should {
    "work" in {
      running(FakeApplication()) {
        println("All done")
        success
      }
    }
  }
}
