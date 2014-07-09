package models

import _root_.helper.TestSupport
import org.specs2.mutable.Specification
import models.json_models.BindingNumber

class BindNumberResponseV1Spec extends Specification with TestSupport {

  "BindNumberV1" should {
    "success when info is active" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("13279491366", "123", "", Some("android"), "1386849160798"))

      response.error_code must equalTo(0)
      response.access_token must not equalTo "0"
      response.member_status must equalTo("已开通")
    }

    "success when info is inactive" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("13408654683", "123", "", Some("android"), "0"))

      response.error_code must equalTo(0)
      response.access_token must not equalTo "0"

    }

    "fail with 1 when number is invalid" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("987654321", "123", "", Some("android"), "1386849160798"))

      response.error_code must equalTo(1)

    }

    "fail with 3 when token is invalid" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("13408654683", "123", "", Some("android"), "999"))

      response.error_code must equalTo(3)

    }

    "fail with 3 for second binding" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("13279491366", "123", "", Some("android"), "1386849160798"))

      response.error_code must equalTo(0)
      response.access_token must not equalTo "0"

      private val response2 = BindNumberResponseV1.handle(new BindingNumber("13279491366", "123", "", Some("android"), "1386849160798"))

      response2.error_code must equalTo(3)

    }

    "fail with 2 when number is expired" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("22222222222", "123", "", Some("android"), "2"))

      response.error_code must equalTo(2)

    }

    "fail with 2 when school is expired" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("22222222223", "123", "", Some("android"), "5"))

      response.error_code must equalTo(2)

    }

    "success for trial parents" in new WithApplication {

      private val response = BindNumberResponseV1.handle(new BindingNumber("22222222224", "123", "", Some("android"), "5"))

      response.error_code must equalTo(0)
      response.member_status must equalTo("试用")

    }
  }
}
