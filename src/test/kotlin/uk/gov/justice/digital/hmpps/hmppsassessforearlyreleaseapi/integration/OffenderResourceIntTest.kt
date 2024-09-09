package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummary
import java.time.LocalDate

const val PRISON_CODE = "BMI"
const val PRISON_NUMBER = "A1234AD"
const val GET_CASE_ADMIN_CASELOAD_URL = "/prison/$PRISON_CODE/case-admin/caseload"
const val GET_CURRENT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment"

class OffenderResourceIntTest : SqsIntegrationTestBase() {

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
      val prisonerNumbers = offenders.map { it.prisonerNumber }
      assertThat(prisonerNumbers).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CD")
    }
  }

  @Nested
  inner class GetCurrentAssessment {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
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
    fun `should return the current assessment for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()

      val assessment = webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(AssessmentSummary::class.java)
        .returnResult().responseBody!!

      assertThat(assessment).isEqualTo(
        AssessmentSummary(
          forename = "FIRST-4",
          surname = "LAST-4",
          prisonNumber = PRISON_NUMBER,
          hdced = LocalDate.of(2020, 10, 25),
          crd = LocalDate.of(2022, 3, 21),
          location = "Birmingham (HMP)",
          status = AssessmentStatus.NOT_STARTED,
        ),
      )
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
