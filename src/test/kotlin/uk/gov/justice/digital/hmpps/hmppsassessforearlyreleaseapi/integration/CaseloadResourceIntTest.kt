package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummary

private const val PRISON_CODE = "BMI"
private const val STAFF_CODE = "STAFF1"
private const val GET_CASE_ADMIN_CASELOAD_URL = "/prison/$PRISON_CODE/case-admin/caseload"
private const val GET_COM_CASELOAD_URL = "/probation/community-offender-manager/staff-code/$STAFF_CODE/caseload"

class CaseloadResourceIntTest : SqsIntegrationTestBase() {

  @Nested
  inner class GetCaseAdminCaseload {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_CASE_ADMIN_CASELOAD_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_CASE_ADMIN_CASELOAD_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_CASE_ADMIN_CASELOAD_URL)
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
    fun `should return offenders at prison with a status of not started`() {
      val offenders = webTestClient.get()
        .uri(GET_CASE_ADMIN_CASELOAD_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(object : ParameterizedTypeReference<List<OffenderSummary>>() {})
        .returnResult().responseBody!!

      assertThat(offenders).hasSize(4)
      val prisonNumbers = offenders.map { it.prisonNumber }
      assertThat(prisonNumbers).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CD")
    }
  }

  @Nested
  inner class GetComCaseload {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_COM_CASELOAD_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_COM_CASELOAD_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_COM_CASELOAD_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/com-caseload.sql",
    )
    @Test
    fun `should return offenders allocated to a COM`() {
      val offenders = webTestClient.get()
        .uri(GET_COM_CASELOAD_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(object : ParameterizedTypeReference<List<OffenderSummary>>() {})
        .returnResult().responseBody!!

      assertThat(offenders).hasSize(2)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AB", "G9524ZF")
      assertThat(offenders.map { it.probationPractitioner }).containsOnly("A Com")
    }
  }

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
