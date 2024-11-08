package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.Name
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.User

private const val USERNAME = "com-user"
private const val GET_STAFF_DETAILS_BY_USERNAME_URL = "/staff?username=$USERNAME"

class StaffResourceIntTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository

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
      deliusMockServer.stubPostStaffDetailsByUsername()

      val comUserDetails = webTestClient.get()
        .uri(GET_STAFF_DETAILS_BY_USERNAME_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(object : ParameterizedTypeReference<User>() {})
        .returnResult().responseBody!!

      Assertions.assertThat(comUserDetails).isEqualTo(comUser)
    }
  }

  private companion object {
    val deliusMockServer = DeliusMockServer()
    val comUser = User(
      id = 2000,
      username = "com-user",
      email = "comuser@probation.gov.uk",
      name = Name(
        forename = "com",
        surname = "user",
      ),
      teams = emptyList(),
      code = "AB00001",
    )

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