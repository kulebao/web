package controllers.V2

import controllers.Secured
import controllers.helper.JsonLogger.loggedJson
import models.V2.NewsV2
import models.V2.NewsV2._
import models.{SuccessResponse, _}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

object NewsControllerV2 extends Controller with Secured {
  implicit val writes = Json.writes[News]
  implicit val writes2 = Json.writes[SuccessResponse]
  implicit val writes4 = Json.writes[ErrorResponse]

  def index(kg: Long, from: Option[Long], to: Option[Long], most: Option[Int], classId: Option[String], tag: Option[Boolean]) = IsLoggedIn {
    u => _ =>
      tag match {
        case Some(true) =>
          val jsons = NewsV2.allSortedWithTag(kg, classId, from, to).take(most.getOrElse(25))
          Ok(loggedJson(jsons))
        case _ =>
          val jsons = News.allSorted(kg, classId, from, to).take(most.getOrElse(25))
          Ok(Json.toJson(jsons))
      }
  }

  def show(kg: Long, newsId: Long, tag: Option[Boolean]) = IsLoggedIn {
    u => _ =>
      tag match {
        case Some(true) =>
          val jsons = NewsV2.showWithTag(kg, newsId)
          Ok(Json.toJson(jsons))
        case _ =>
          News.findById(kg, newsId).fold(NotFound(""))(news =>
            Ok(Json.toJson(news)))
      }
  }


  def update(kg: Long, employeeId: String, newsId: Long) = IsLoggedIn(parse.json) {
    u =>
      request =>
        request.body.validate[News].map {
          case (news) if !Employee.canAccess(Some(employeeId), kg) =>
            Forbidden(Json.toJson(ErrorResponse("您无权修改学校公告。(no authority to update)")))
          case (news) if news.news_id.getOrElse(0) != newsId =>
            BadRequest(Json.toJson(ErrorResponse("公告ID不匹配URL.(url and news id are not matched)")))
          case (news) =>
            Ok(Json.toJson(NewsV2.update(news)))
        }.recoverTotal {
          e => BadRequest("Detected error:" + JsError.toFlatJson(e))
        }
  }

  def create(kg: Long, employeeId: String) = IsLoggedIn(parse.json) {
    u => request =>
      request.body.validate[News].map {
        case (news) if !Employee.canAccess(Some(employeeId), kg) =>
          Forbidden(Json.toJson(ErrorResponse("您无权创建学校公告。(no authority to create)")))
        case (news) if !news.publisher_id.getOrElse("").equals(employeeId) =>
          Forbidden(Json.toJson(ErrorResponse("发布ID错误。(publisher id does not match)")))
        case (news) =>
          Ok(Json.toJson(NewsV2.create(news.copy(news_id = None))))
      }.recoverTotal {
        e => BadRequest("Detected error:" + JsError.toFlatJson(e))
      }
  }

  def delete(kg: Long, employeeId: String, newsId: Long) = IsLoggedIn {
    u => _ =>
      Employee.canAccess(Some(employeeId), kg) match {
        case false => Forbidden(Json.toJson(ErrorResponse("您无权删除学校公告。(no authority to delete)")))
        case true =>
          NewsV2.delete(newsId)
          Ok(Json.toJson(new SuccessResponse))
      }

  }

  def indexWithNonPublished(kg: Long, employeeId: String, class_id: Option[String], restrict: Option[Boolean], from: Option[Long], to: Option[Long], most: Option[Int]) = IsLoggedIn {
    u => _ =>
      Employee.canAccess(Some(employeeId), kg) match {
        case false => Forbidden(Json.toJson(ErrorResponse("您无权查看学校公告。(no authority to read)")))
        case true =>
          val jsons = NewsV2.allIncludeNonPublished(kg, class_id, restrict.getOrElse(false), from, to).take(most.getOrElse(25))
          Ok(Json.toJson(jsons))
      }
  }

}