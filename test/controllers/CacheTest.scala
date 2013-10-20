package controllers

import model._
import model.training._
import org.slf4j.LoggerFactory
import org.specs2.mutable._
import play.api.cache.Cache
import play.api.Play.current
import play.api.test.Helpers._
import play.api.test.FakeApplication

class CacheTest extends Specification {
  val Logger = LoggerFactory.getLogger("test")

  "The Cache" should {
    "support simple read and write" in {
      running(new FakeApplication) {
        Cache.set("key", "value", domainCacheTime)
        val result = Cache.get("key")
        result.get == "value"
      }
    }
  }

  "The Group cache" should { // fails because DB is not set up for testing
    "work for findById" in {
      running(new FakeApplication) {
        val id: Long = -123
        val group = Group("test", id)
        Groups.remove(group) // just making sure

        val deletedGroup = Cache.get("group"+id)
        deletedGroup==None

        val modifiedGroup = Groups.upsert(group)
        Groups.findById(modifiedGroup.id)
        val result = Cache.get("group"+id)
        result.isDefined
      }
    }

    /*"work for findById" in {
      running(new FakeApplication) {
        Cache.set("key", "value", domainCacheTime)
        val result = Cache.get("key")
        result.get == "value"
      }
    }*/
  }
}
