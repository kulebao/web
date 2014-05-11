package models

import play.api.db.DB
import anorm._
import anorm.SqlParser._
import anorm.~
import models.json_models.CheckNotification
import models.json_models.CheckInfo
import play.api.Play.current
import models.helper.RangerHelper.rangerQueryWithField
import org.joda.time.DateTime
import play.Logger

case class DailyLog(timestamp: Long, notice_type: Int, child_id: String, record_url: String, parent_name: String)

object DailyLog {

  def generateClassQuery(classes: String) = " child_id IN ( SELECT child_id FROM childinfo WHERE school_id={kg} AND class_id IN (%s)) ".format(classes)

  def lastCheckInClasses(kg: Long, classes: String) = DB.withConnection {
    implicit c =>
      SQL("SELECT * FROM dailylog d, " +
        " (SELECT MAX(check_at) last_check, child_id FROM `dailylog` WHERE school_id={kg} AND "+ generateClassQuery(classes) +
        "  AND check_at > {today} GROUP BY child_id) a " +
        " WHERE d.child_id=a.child_id AND d.check_at=a.last_check ")
        .on(
          'kg -> kg.toString,
          'today -> DateTime.now().toLocalDate.toDateTimeAtStartOfDay.toInstant.getMillis
        ).as(simple *)
  }

  val simple = {
    get[String]("child_id") ~
      get[Long]("check_at") ~
      get[String]("record_url") ~
      get[Int]("notice_type") ~
      get[String]("parent_name") map {
      case child_id ~ timestamp ~ url ~ notice_type ~ name =>
        new DailyLog(timestamp, notice_type, child_id, url, name)
    }
  }

  def all(kg: Long, childId: String, from: Option[Long], to: Option[Long]) = DB.withConnection {
    implicit c =>
      SQL("select * from dailylog where child_id={child_id} and school_id={school_id} " + rangerQueryWithField(from, to, Some("check_at")))
        .on(
          'child_id -> childId,
          'school_id -> kg.toString,
          'from -> from,
          'to -> to
        ).as(simple *)
  }


  def create(notifications: List[CheckNotification], request: CheckInfo) = DB.withConnection {
    implicit c =>
      notifications match {
        case x::xs =>
          SQL("insert into dailylog (child_id, record_url, check_at, card_no, notice_type, school_id, parent_name) " +
            "values ({child_id}, {url}, {check_at}, {card_no}, {notice_type}, {school_id}, {parent_name})")
            .on(
              'child_id -> x.child_id,
              'url -> x.record_url,
              'check_at -> x.timestamp,
              'card_no -> request.card_no,
              'notice_type -> request.notice_type,
              'school_id -> request.school_id,
              'parent_name -> x.parent_name
            ).executeInsert()
        case _ => None
      }
  }

}
