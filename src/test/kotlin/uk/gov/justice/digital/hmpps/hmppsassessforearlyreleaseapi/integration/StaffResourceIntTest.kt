package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.User

private const val USERNAME = "com-user"
private const val GET_STAFF_DETAILS_BY_USERNAME_URL = "/staff?username=$USERNAME"
private const val USER_CODE = "AB00001"
private const val USER_EMAIL = "comuser@probation.gov.uk"

class StaffResourceIntTest : SqsIntegrationTestBase() {

  @Nested
  inner class GetStaffDetailsByUsername {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_STAFF_DETAILS_BY_USERNAME_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_STAFF_DETAILS_BY_USERNAME_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_STAFF_DETAILS_BY_USERNAME_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return staff details with a username`() {
      deliusMockServer.stubGettStaffDetailsByUsername(USERNAME)

      val comUserDetails = webTestClient.get()
        .uri(GET_STAFF_DETAILS_BY_USERNAME_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(object : ParameterizedTypeReference<User>() {})
        .returnResult().responseBody!!

      assertThat(comUserDetails.username).isEqualTo(USERNAME)
      assertThat(comUserDetails.code).isEqualTo(USER_CODE)
      assertThat(comUserDetails.email).isEqualTo(USER_EMAIL)
    }

    @Test
    fun `should throw 404 when user not found`() {
      deliusMockServer.stubPostStaffDetailsByUsernameDataNotFound()

      val exception = webTestClient.get()
        .uri(GET_STAFF_DETAILS_BY_USERNAME_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody

      assertThat(exception?.status).isEqualTo(HttpStatus.NOT_FOUND.value())
      assertThat(exception?.userMessage).isEqualTo("Could not find entity: Cannot find staff with username $USERNAME")
    }
  }

  private companion object {
    val deliusMockServer = DeliusMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      deliusMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      deliusMockServer.stop()
    }
  }
}
