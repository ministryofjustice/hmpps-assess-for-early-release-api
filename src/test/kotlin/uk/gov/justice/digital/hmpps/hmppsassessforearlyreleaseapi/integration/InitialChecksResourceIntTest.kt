package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import java.nio.charset.StandardCharsets

private const val GET_INITIAL_CHECKS_STATUS_URL =
  "/offender/${TestData.PRISON_NUMBER}/current-assessment/initial-checks"

class InitialChecksResourceIntTest : SqsIntegrationTestBase() {

  @Nested
  inner class GetInitialCheck {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_INITIAL_CHECKS_STATUS_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_INITIAL_CHECKS_STATUS_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_INITIAL_CHECKS_STATUS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should return the initial checks for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()

      webTestClient.get()
        .uri(GET_INITIAL_CHECKS_STATUS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("initial-checks"), true)
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
