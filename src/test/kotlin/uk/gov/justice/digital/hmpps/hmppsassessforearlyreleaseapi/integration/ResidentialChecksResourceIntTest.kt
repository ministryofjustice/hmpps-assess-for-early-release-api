package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import java.nio.charset.StandardCharsets

private const val GET_RESIDENTIAL_CHECKS_VIEW_URL = "/offender/$PRISON_NUMBER/current-assessment/residential-checks"

class ResidentialChecksResourceIntTest : SqsIntegrationTestBase() {

  @Nested
  inner class GetResidentialChecksView {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-checks-complete.sql",
    )
    @Test
    fun `should return the residential checks for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()

      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("residential-checks-view"), true)
    }
  }

  private fun serializedContent(name: String) =
    this.javaClass.getResourceAsStream("/test_data/responses/$name.json")!!.bufferedReader(
      StandardCharsets.UTF_8,
    ).readText()

  private companion object {
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonRegisterMockServer.stop()
    }
  }
}
