package controllers

import _root_.helper.TestSupport
import models.{LoginNameCheck, ChildInfo}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import models.Employee.writeLoginNameCheck
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithServer}

@RunWith(classOf[JUnitRunner])
class EmployeeControllerSpec extends Specification with TestSupport {
  implicit val writes = Json.writes[ChildInfo]

  def operatorRequest(method: String, url: String) = {
    FakeRequest(method, url).withSession("id" -> "3_93740362_9972", "username" -> "operator")
  }

  def principalLoggedRequest(method: String, url: String) = {
    FakeRequest(method, url).withSession("id" -> "3_93740362_1122", "username" -> "e0001")
  }

  def twoClassesManagerLoggedRequest(method: String, url: String) = {
    FakeRequest(method, url).withSession("id" -> "3_93740362_3344", "username" -> "e0002")
  }

  def noAccessLoggedRequest(method: String, url: String) = {
    FakeRequest(method, url).withSession("id" -> "3_93740362_9977", "username" -> "e0003")
  }

  "Employee" should {
    "check authentication first" in new WithServer {

      val employeeResponse = route(FakeRequest(GET, "/employee")).get

      status(employeeResponse) must equalTo(UNAUTHORIZED)

    }

    "show all employees to operator" in new WithServer {

      val response = route(operatorRequest(GET, "/employee")).get

      status(response) must equalTo(OK)

      val jsonResponse: JsValue = Json.parse(contentAsString(response))
      jsonResponse match {
        case JsArray(arr) =>
          arr.length must greaterThanOrEqualTo(6)
        case _ => failure
      }
    }

    "show nothing to principals" in new WithServer {

      val response = route(principalLoggedRequest(GET, "/employee")).get

      status(response) must equalTo(UNAUTHORIZED)

    }

    "be able to delete in school employees" in new WithServer {

      val response = route(principalLoggedRequest(DELETE, "/kindergarten/93740362/employee/23258249821")).get

      status(response) must equalTo(OK)

      val response2 = route(principalLoggedRequest(GET, "/kindergarten/93740362/employee/23258249821")).get

      status(response2) must equalTo(NOT_FOUND)

    }

    "never delete the account itself" in new WithServer {

      val response = route(principalLoggedRequest(DELETE, "/kindergarten/93740362/employee/13258249821")).get

      status(response) must equalTo(UNAUTHORIZED)

    }

    "accept login name check when name has not been used" in new WithApplication {
      private val requestBody = Json.toJson(LoginNameCheck(None, "nonexists", None))

      val checkResponse = route(principalLoggedRequest(POST, "/api/v1/kindergarten/93740362/login_name_check").withJsonBody(requestBody)).get

      status(checkResponse) must equalTo(OK)
      contentType(checkResponse) must beSome.which(_ == "application/json")

      val response: JsValue = Json.parse(contentAsString(checkResponse))
      (response \ "error_code").as[Int] must equalTo(0)

    }

    "accept login name when name and id are matching" in new WithApplication {
      private val requestBody = Json.toJson(LoginNameCheck(None, "e0001", Some("3_93740362_1122")))

      val checkResponse = route(principalLoggedRequest(POST, "/api/v1/kindergarten/93740362/login_name_check").withJsonBody(requestBody)).get

      status(checkResponse) must equalTo(OK)
      contentType(checkResponse) must beSome.which(_ == "application/json")

      val response: JsValue = Json.parse(contentAsString(checkResponse))
      (response \ "error_code").as[Int] must equalTo(0)

    }

    "reject login name when name and id are not matching" in new WithApplication {
      private val requestBody = Json.toJson(LoginNameCheck(None, "e0001", Some("2_93740362_456")))

      val checkResponse = route(principalLoggedRequest(POST, "/api/v1/kindergarten/93740362/login_name_check").withJsonBody(requestBody)).get

      status(checkResponse) must equalTo(OK)
      contentType(checkResponse) must beSome.which(_ == "application/json")

      val response: JsValue = Json.parse(contentAsString(checkResponse))
      (response \ "error_code").as[Int] must equalTo(51)

    }

    "reject login name when name exists and id is missing" in new WithApplication {
      private val requestBody = Json.toJson(LoginNameCheck(None, "e0001", None))

      val checkResponse = route(principalLoggedRequest(POST, "/api/v1/kindergarten/93740362/login_name_check").withJsonBody(requestBody)).get

      status(checkResponse) must equalTo(OK)
      contentType(checkResponse) must beSome.which(_ == "application/json")

      val response: JsValue = Json.parse(contentAsString(checkResponse))
      (response \ "error_code").as[Int] must equalTo(51)

    }
  }

  "reject login name when name belongs to agent" in new WithApplication {
      private val requestBody = Json.toJson(LoginNameCheck(None, "a0001", None))

      val checkResponse = route(principalLoggedRequest(POST, "/api/v1/kindergarten/93740362/login_name_check").withJsonBody(requestBody)).get

      status(checkResponse) must equalTo(OK)
      contentType(checkResponse) must beSome.which(_ == "application/json")

      val response: JsValue = Json.parse(contentAsString(checkResponse))
      (response \ "error_code").as[Int] must equalTo(52)

    }

  "accept agent login name when name belongs to agent however matches the id" in new WithApplication {
      private val requestBody = Json.toJson(LoginNameCheck(Some(1), "a0001", None))

      val checkResponse = route(principalLoggedRequest(POST, "/api/v1/kindergarten/93740362/login_name_check").withJsonBody(requestBody)).get

      status(checkResponse) must equalTo(OK)
      contentType(checkResponse) must beSome.which(_ == "application/json")

      val response: JsValue = Json.parse(contentAsString(checkResponse))
      (response \ "error_code").as[Int] must equalTo(0)

    }

}
