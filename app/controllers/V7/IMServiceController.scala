package controllers.V7

import controllers.Secured
import controllers.helper.JsonLogger._
import models.ErrorResponse.writeErrorResponse
import models.V7.IMToken.{writeIMBasicRes, writeIMToken}
import models.V7.{IMBanUser, IMToken}
import models.V7.IMToken.readsIMBanUser
import models._
import play.Logger
import play.api.libs.json.{JsError, Json}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object IMServiceController extends Controller with Secured {
  def token(kg: Long, id: Long) = IsLoggedInAsync { username =>
    request =>
      Logger.info(s"username = $username")
      val imAccount: Option[IMAccount] = createRequestByUser(username, kg, id)
      Logger.info(s"imAccount = $imAccount")
      imAccount.fold(Future(InternalServerError(Json.toJson(ErrorResponse("提供的信息不准确, 您无权查看该用户的聊天token.(error in querying user information)"))))) {
        case account =>
          IMToken.retrieveIMToken(kg, account).map {
            case Some(imToken) =>
              Ok(loggedJson(imToken))
            case None =>
              InternalServerError(Json.toJson(ErrorResponse("获取IM提供商Token出错.(error in retrieving IM token)")))
          }
      }
  }

  def createRequestByUser(username: String, kg: Long, id: Long): Option[IMAccount] = {
    whatIfParents(username, kg, id) orElse whatIfEmployees(username, kg, id)
  }

  def whoIsRequesting(username: String, kg: Long): Option[IMAccount] = {
    Parent.phoneSearch(username).filter(_.school_id == kg) orElse
      Employee.findByLoginName(username).filter(_.school_id == kg)
  }

  def whatIfParents(username: String, kg: Long, id: Long): Option[IMAccount] = {
    Parent.phoneSearch(username).filter(_.school_id == kg).filter(_.id == Some(id))
  }

  def whatIfEmployees(loginName: String, kg: Long, id: Long): Option[IMAccount] = {
    Employee.findByLoginName(loginName).filter(_.school_id == kg).filter(_.uid == Some(id))
  }

  def classGroup(kg: Long, id: Long) = IsLoggedInAsync { username =>
    request =>
      Logger.info(s"username = $username")
      val imAccount: Option[IMAccount] = whoIsRequesting(username, kg)
      Logger.info(s"imAccount = $imAccount")
      IMToken.classGroup(kg, username, id, imAccount).map {
        case Some(imToken) =>
          Ok(loggedJson(imToken))
        case None =>
          InternalServerError(Json.toJson(ErrorResponse("创建班级聊天群出错.(error in creating class IM group)")))
      }
  }

  def banUser(kg: Long, class_id: Int) = IsLoggedInAsync(parse.json) { username =>
    request =>
      Logger.info(s"username = $username")
      val imAccount: Option[IMAccount] = whoIsRequesting(username, kg)
      Logger.info(s"imAccount = $imAccount")

      request.body.validate[List[IMBanUser]].map {
        case all =>
          IMToken.ban(kg, class_id, imAccount, all).map {
            case res if res.size == all.size =>
              Ok(Json.toJson(new SuccessResponse))
            case res =>
              InternalServerError(Json.toJson(ErrorResponse(s"禁言用户出错.(error in creating class IM group, ${res})")))
          }
      }.recoverTotal {
        e => Future.successful(BadRequest("Detected error:" + JsError.toFlatJson(e)))
      }


  }
//
//  def allowUser(kg: Long, class_id: Int, id: Long) = IsLoggedInAsync { username =>
//    request =>
//      Logger.info(s"username = $username")
//      val imAccount: Option[IMAccount] = whoIsRequesting(username, kg)
//      Logger.info(s"imAccount = $imAccount")
//      IMToken.classGroup(kg, username, id, imAccount).map {
//        case Some(imToken) =>
//          Ok(loggedJson(imToken))
//        case None =>
//          InternalServerError(Json.toJson(ErrorResponse("取消用户禁言出错.(error in creating class IM group)")))
//      }
//  }
//
//  def bannedUserList(kg: Long, class_id: Int) = IsLoggedInAsync { username =>
//    request =>
//      Logger.info(s"username = $username")
//      val imAccount: Option[IMAccount] = whoIsRequesting(username, kg)
//      Logger.info(s"imAccount = $imAccount")
//      IMToken.bannedUserList(kg, username, class_id, imAccount).map {
//        case Some(imToken) =>
//          Ok(loggedJson(imToken))
//        case None =>
//          InternalServerError(Json.toJson(ErrorResponse("查看班级聊天群禁言名单出错.(error in querying class IM group banned users)")))
//      }
//  }
}
