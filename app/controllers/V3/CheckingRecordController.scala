package controllers.V3

import controllers.Secured
import models.V3.{CheckingRecordV3, ErrorCheckingRecordV3}
import models.json_models.CheckingMessage.checkInfoReads
import models.{Relationship, Card, ErrorResponse, SuccessResponse}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.Controller

object CheckingRecordController extends Controller with Secured {

  import models.V3.CheckingRecordV3.condition

  def index(kg: Long, from: Option[Long], to: Option[Long], most: Option[Int]) = IsLoggedIn { u => _ =>
    Ok(Json.toJson(CheckingRecordV3.index(kg, from, to, most)))
  }

  def show(kg: Long, id: Long) = IsLoggedIn { u => _ =>
    CheckingRecordV3.show(kg, id) match {
      case Some(x) =>
        Ok(Json.toJson(x))
      case None =>
        NotFound(Json.toJson(ErrorResponse(s"没有ID为${id}的刷卡记录。(No such check-in record)")))
    }
  }

  def create(kg: Long) = IsLoggedIn(parse.json) { u => request =>
    request.body.validate[CheckingRecordV3].map {
      case (s) if s.check_info.school_id != kg =>
        BadRequest(Json.toJson(ErrorResponse("学校不匹配(School_id is not matched)", 2)))
      case (s) if !Relationship.cardExists(s.check_info.card_no, None) =>
        BadRequest(Json.toJson(ErrorResponse("卡号不存在(Card number does not exist)", 3)))
      case (s) =>
        s.create match {
          case Some(created) =>
            Ok(Json.toJson(created))
          case None =>
            InternalServerError(Json.toJson(ErrorResponse("创建刷卡记录失败.(Error in creating checking record v3)", 4)))
        }

    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }

  def delete(kg: Long, id: Long) = IsLoggedIn { u => _ =>
    CheckingRecordV3.deleteById(kg, id)
    Ok(Json.toJson(new SuccessResponse()))
  }

  def push(kg: Long) = IsLoggedIn { u => _ =>
    Ok(Json.toJson(new SuccessResponse()))
  }
}

/*
* create table TrackingInfo.dbo.CardRecord(
SysNO int identity(1,1),系统编号
EnrollNumber varchar(20),工号（已无使用价值，可弃，以前用于对应打卡人，现应改成卡号cardNumber）
RecordTime datetime,刷卡时间
CreateUserSysNO int,创建者系统编号
Memo nvarchar(500),备注
PicPath varchar(max),实时照片路径
WorkCheck char(1)实时算出的考勤结果，如果不用实时计算的结果，可弃
)


*/

object ErrorCheckingRecordController extends Controller with Secured {

  def index(kg: Long, from: Option[Long], to: Option[Long], most: Option[Int]) = IsLoggedIn { u => _ =>
    Ok(Json.toJson(ErrorCheckingRecordV3.index(kg, from, to, most)))
  }

  def show(kg: Long, id: Long) = IsLoggedIn { u => _ =>
    ErrorCheckingRecordV3.show(kg, id) match {
      case Some(x) =>
        Ok(Json.toJson(x))
      case None =>
        NotFound(Json.toJson(ErrorResponse(s"没有ID为${id}的错误刷卡记录。(No such error check-in record)")))
    }
  }

  def create(kg: Long) = IsLoggedIn(parse.json) { u => request =>
    request.body.validate[CheckingRecordV3].map {
      case (s) if s.check_info.school_id != kg =>
        BadRequest(Json.toJson(ErrorResponse("学校ID不匹配(school_id is not matched)", 2)))
      case (s) =>
        Ok(Json.toJson(s.createErrorRecord))
    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }

  def delete(kg: Long, id: Long) = IsLoggedIn { u => _ =>
    ErrorCheckingRecordV3.deleteById(kg, id)
    Ok(Json.toJson(new SuccessResponse()))
  }
}