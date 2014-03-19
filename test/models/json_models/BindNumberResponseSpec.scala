package models.json_models

import org.specs2.mutable.Specification
import helper.TestSupport

class BindNumberResponseSpec extends Specification with TestSupport {

  "BindNumber" should {
    "success when info is active" in new WithApplication {

      private val response = BindNumberResponse.handle(new BindingNumber("13279491366", "123", "", Some("android"), "1386849160798"))

      response.error_code must equalTo(0)

    }

    "success when info is inactive" in new WithApplication {

      private val response = BindNumberResponse.handle(new BindingNumber("13408654683", "123", "", Some("android"), "0"))

      response.error_code must equalTo(0)
      response.access_token must not equalTo "0"

    }

    "fail when number is invalid" in new WithApplication {

      private val response = BindNumberResponse.handle(new BindingNumber("987654321", "123", "", Some("android"), "1386849160798"))

      response.error_code must equalTo(1)

    }

    "fail when token is invalid" in new WithApplication {

      private val response = BindNumberResponse.handle(new BindingNumber("13402815317", "123", "", Some("android"), "0"))

      response.error_code must equalTo(1)

    }

    "fail when number is expired" in new WithApplication {

      private val response = BindNumberResponse.handle(new BindingNumber("22222222222", "123", "", Some("android"), "2"))

      response.error_code must equalTo(2)

    }

    "fail when school is expired" in new WithApplication {

      private val response = BindNumberResponse.handle(new BindingNumber("22222222223", "123", "", Some("android"), "5"))

      response.error_code must equalTo(2)

    }
  }
}