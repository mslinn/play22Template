package controllers

import collection.JavaConverters._
import java.io.File
import java.nio.charset.Charset
import model._
import model.training._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import scala.util.{Try, Failure, Success}
import service.aws.{AwsCors, AWSPolicy, AWSUpload}
import service.aws.AWSUpload._

import org.apache.http.client.methods.{HttpPut, HttpPost}
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.{StringBody, FileBody}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.apache.http.HttpResponse
import controllers.UploadController.SignedAndEncoded

class UploadTest extends Specification {
  val Logger = LoggerFactory.getLogger("test")

  sealed trait UploadPurpose
  case object Asset extends UploadPurpose
  case object Homework extends UploadPurpose

  val fileName = "uploadMe.png"
  val resourcesDir = "test/resources"

  val contentLength = new File(fileName).length
  val key = s"uploadDirectory/$fileName"
  val file = new File(resourcesDir, fileName)
  "uploadAsset" should {
    "sign, encode and upload publicly readable file to AWS S3" in new WithApplication {
      // Only lazy vals and defs are allowed in this block; no vals or any other code blocks, otherwise delayedInit() will
      // get invoked twice and therefore around() will get invoked twice
      createBuckets(Context.pubConf)
      uploadFile(Asset)
      uploadFile(Homework)
    }
  }

  def uploadFile(uploadPurpose: UploadPurpose): Unit = {
    val (acl: String, bucketName: String, user: User) = uploadPurpose match {
      case Asset =>
        (publicAcl, Context.pubConf.uploadBucketName, Context.instructor1)

      case Homework =>
        (privateAcl, Context.pubConf.homeworkBucketName, Context.student1)
    }
    val uploadUrl = s"http://$bucketName.s3-website-us-east-1.amazonaws.com" // key is NOT part of the url
    val awsUpload = new AWSUpload(bucketName)(user)
    val sae: SignedAndEncoded =
      awsUpload.getSignedAndEncoded(fileName, contentLength, Context.pubConf.awsSecretKey, acl=acl)
    sae.encodedPolicy shouldNotEqual ""
    sae.signedPolicy  shouldNotEqual ""
    sae.contentType   shouldEqual    "image/png"

    val params = Map(
      "key"            -> key,
      "AWSAccessKeyId" -> Context.pubConf.awsAccessKey,
      "acl"            -> acl,
      "policy"         -> sae.encodedPolicy,
      "signature"      -> sae.signedPolicy,
      "Content-Type"   -> sae.contentType
    )
    uploadViaHttpClient(uploadUrl, params, file) match {
      case Success(body) =>
        if (body.contains("<Error>")) {
          failure(body)
        } else {
          println(body)
          success
        }
      case Failure(throwable) =>
        failure("No response")
    }
    ()
  }

  def uploadViaHttpClient(uploadUrl: String, params: Map[String, String], file: File): Try[String] = {
    def responseContent(response: HttpResponse): String = {
      val resEntity = response.getEntity
      if (resEntity != null)
        Logger.debug("  Response content length: " + resEntity.getContentLength)
      EntityUtils.consume(resEntity)
      val result = try {
        io.Source.fromInputStream(resEntity.getContent).mkString
      } catch {
        case e: Exception => e.getMessage
      }
      result
    }

    val httpClient = new DefaultHttpClient
    try {
      val httpPost = new HttpPost(uploadUrl)
      val multipartEntity = new MultipartEntity()
      params.foreach { param =>
        val stringBody = new StringBody(param._2, Charset.forName("UTF-8"))
        multipartEntity.addPart(param._1, stringBody)
        Logger.debug(s"  Adding ${param._1}: ${param._2}")
      }
      val fileBody = new FileBody(file)
      multipartEntity.addPart("file", fileBody)
      Logger.debug(s"  Adding file: ${file.getAbsolutePath}")
      httpPost.setEntity(multipartEntity)

      Logger.debug("Executing request " + httpPost.getRequestLine)
      val response = httpClient.execute(httpPost)
      Logger.debug("  response.getStatusLine=" + response.getStatusLine)
      val statusCode = response.getStatusLine.getStatusCode
      if (statusCode>=300) {
        Logger.error(s"  statusCode $statusCode\n${responseContent(response)}")
        failure(s"  statusCode $statusCode\n${responseContent(response)}")
      } else {
        Success(responseContent(response))
      }
    } catch {
      case e: Exception =>
        showStackTrace(e)
        Failure(e)
    }
  }

  def createBuckets(pubConf: PublisherConfig): Unit = {
    try {
      if (!pubConf.bucketExists)
        pubConf.s3.createBucket(pubConf.bucketName)
      pubConf.s3.enableWebsite(pubConf.bucketName)
      AwsCors.setCors(pubConf.s3, pubConf.bucketName)
    } catch {
      case e: Exception =>
        println(s"createBuckets: ${e.toString}")
        showStackTrace(e)
        try {
          // pubConf.s3.deleteBucket(pubConf.bucketName)
        } catch {
          case ignored: Throwable =>
            Logger.debug(ignored.toString)
        }
        failure(s"Exception creating ${pubConf.uploadBucketName}; $e")
    }

    try {
      if (!pubConf.uploadBucketExists)
        pubConf.s3.createBucket(pubConf.uploadBucketName)
      pubConf.s3.enableWebsite(pubConf.uploadBucketName) // Must enable website!
      AWSPolicy.setUploadBucketPolicy(pubConf)
      AwsCors.setCors(pubConf.s3, pubConf.uploadBucketName)
    } catch {
      case e: Exception =>
        Logger.debug(e.toString)
        showStackTrace(e)
        try {
          // pubConf.s3.deleteBucket(pubConf.uploadBucketName)
        } catch {
          case ignored: Throwable =>
            Logger.debug(ignored.toString)
        }
        failure(s"Exception creating ${pubConf.uploadBucketName}; $e")
    }

    try {
      if (!pubConf.homeworkBucketExists)
        pubConf.s3.createBucket(pubConf.homeworkBucketName)
      pubConf.s3.enableWebsite(pubConf.homeworkBucketName) // Must enable website!
      AWSPolicy.setHomeworkBucketPolicy(pubConf)
      AwsCors.setCors(pubConf.s3, pubConf.homeworkBucketName)
    } catch {
      case e: Exception =>
        Logger.debug(e.toString)
        showStackTrace(e)
        try {
          // pubConf.s3.deleteBucket(pubConf.homeworkBucketName)
        } catch {
          case ignored: Throwable =>
            Logger.debug(ignored.toString)
        }
        failure(s"Exception creating ${pubConf.homeworkBucketName}; $e")
    }
    ()
  }

  def showStackTrace(e: Exception) = {
    val trace = e.getStackTrace
    val lines = trace.map(_.toString).filterNot(_.startsWith("scala.")).take(8).mkString("\n")
    println(s"$lines\n... maximum 8 of ${trace.size} stack elements shown")
  }
}
