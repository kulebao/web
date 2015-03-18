package models

import play.api.db.DB
import anorm._
import play.api.Logger
import anorm.SqlParser._
import anorm.~
import scala.Some
import models.json_models.BindingNumber
import play.api.Play.current
import models.PushableNumber.{updateTokenAfterBinding, convertPhoneToPushableNumber}
import models.MemberPhoneNumber.convertPhoneToMemberPhoneNumber


case class BindingResponseV1(error_code: Int,
                              access_token: String="",
                              username: String="",
                              account_name: String="",
                              school_id: Long=0,
                              school_name: String="",
                              member_status: String="")

case class MemberStatus(status: Int){
  def readable = status match {
    case 0 => "free"
    case 1 => "paid"
    case 2 => "suspended"
  }
}

object BindingV1 {

  implicit def convertToMemberStatus(status: Int) = MemberStatus(status)

  def response(updateTime: Long) = {
    get[String]("accountid") ~
      get[String]("parentinfo.name") ~
      get[String]("school_id") ~
      get[String]("schoolinfo.name") ~
      get[Int]("member_status") ~
      get[Int]("active") map {
      case accountid ~ parent ~ schoolId ~ schoolName ~ account_status ~ active =>
        BindingResponseV1(0, updateTime.toString, parent, accountid, schoolId.toLong, schoolName, account_status.readable)
    }
  }
  def apply(request: BindingNumber) = DB.withConnection {
    implicit c =>
      val row = SQL("select a.*, p.name, p.school_id, s.name, member_status " +
        "from accountinfo a, parentinfo p, schoolinfo s, chargeinfo c " +
        "where s.school_id=p.school_id and a.accountid = p.phone and c.school_id=p.school_id " +
        "and c.status=1 and p.status=1 and member_status in (0, 1) " +
        "and accountid={accountid} and pwd_change_time={token}")
        .on(
          'accountid -> request.phonenum,
          'token -> request.access_token.toLong
        ).as(response(request.access_token.toLong) singleOpt)
      Logger.info(row.toString)
      row match {
        case Some(r) => r
        case res if res.isEmpty && request.phonenum.existsDisregardingToken("0,1") =>
          BindingResponseV1(3)
        case res if res.isEmpty && request.phonenum.isExpired("2") =>
          BindingResponseV1(2)
        case None =>
          BindingResponseV1(1)
      }
  }
}
