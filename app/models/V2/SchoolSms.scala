package models.V2

import anorm.SqlParser._
import anorm._
import models.SchoolConfig
import play.api.db.DB
import play.api.libs.json.Json
import play.api.libs.ws.WS

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.api.Play.current

case class SchoolSms(school_id: Long, available: Long, consumers: Long, pagination: Long = 65)

object SchoolSms {
  implicit val readSchoolSms = Json.reads[SchoolSms]
  implicit val writeSchoolSms = Json.writes[SchoolSms]

  def config(schoolId: Long, classIds: Option[Int]): Future[SchoolSms] = {
    val account = SchoolConfig.valueOfKey(schoolId, "smsPushAccount").getOrElse("")
    val password = SchoolConfig.valueOfKey(schoolId, "smsPushPassword").getOrElse("")
    WS.url(s"http://mb345.com:999/ws/SelSum.aspx?CorpID=${account}&Pwd=${password}").get() map {
      case (res) if res.body.toInt > 0 =>
        SchoolSms(schoolId, res.body.toInt, allFeaturePhones(schoolId, classIds).size)
      case _ =>
        SchoolSms(schoolId, 0, allFeaturePhones(schoolId, classIds).size)
    }
  }

  def allFeaturePhones(schoolId: Long, classIds: Option[Int]): List[String] = DB.withConnection {
    implicit c =>
      SQL("select p.phone from parentinfo p, parentext ex where p.uid = ex.base_id and school_id={kg} " +
        s" and p.status=1 and sms_push=1 ${classRange(classIds)}")
        .on(
          'kg -> schoolId.toString,
          'range -> classIds
        ).as(get[String]("phone") *)
  }

  def classRange(classIds: Option[Int]) = classIds match {
    case Some(range) if range != 0 => s" and p.parent_id in (select p.parent_id from parentinfo p, relationmap r, childinfo c where p.parent_id = r.parent_id and c.child_id = r.child_id  and class_id ={range})"
    case _ => ""
  }

}
