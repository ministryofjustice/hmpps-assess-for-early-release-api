package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.GovUkMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import java.time.LocalDate

private const val PRISON_CODE = "BMI"
private const val STAFF_CODE = "STAFF1"
private const val GET_CASE_ADMIN_CASELOAD_URL = "/prison/$PRISON_CODE/case-admin/caseload"
private const val GET_COM_CASELOAD_URL = "/probation/community-offender-manager/staff-code/$STAFF_CODE/caseload"
private const val GET_DECISION_MAKER_CASELOAD_URL = "/prison/$PRISON_CODE/decision-maker/caseload"

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
      // Given
      govUkMockServer.stubGetBankHolidays()
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val response = webTestClient.get()
        .uri(GET_CASE_ADMIN_CASELOAD_URL)
        .headers(setAuthorisation(roles = roles))
        .exchange()

      // Then
      response.expectStatus().isOk

      val offenders = response.expectBody(object : ParameterizedTypeReference<List<OffenderSummary>>() {})
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(4)
      val prisonNumbers = offenders.map { it.prisonNumber }
      assertThat(prisonNumbers).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CD")
      assertPostponeDetails(offenders, "A1234AA")
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
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      // When
      val response = webTestClient.get()
        .uri(GET_COM_CASELOAD_URL)
        .headers(setAuthorisation(roles = roles))
        .exchange()

      // Then
      response.expectStatus().isOk

      val offenders = response.expectBody(object : ParameterizedTypeReference<List<OffenderSummary>>() {})
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(2)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AB", "G9524ZF")
      assertThat(offenders.map { it.probationPractitioner }).containsOnly("A Com")
      assertPostponeDetails(offenders, "A1234AB")
    }
  }

  @Nested
  inner class GetDecisionMakerCaseload {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_DECISION_MAKER_CASELOAD_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_DECISION_MAKER_CASELOAD_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_DECISION_MAKER_CASELOAD_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/decision-maker-caseload.sql",
    )
    @Test
    fun `should return offenders allocated to a Decision Maker`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val response = webTestClient.get()
        .uri(GET_DECISION_MAKER_CASELOAD_URL)
        .headers(setAuthorisation(roles = roles))
        .exchange()

      // Then
      response.expectStatus().isOk

      val offenders = response.expectBody(object : ParameterizedTypeReference<List<OffenderSummary>>() {})
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(2)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AB", "A1234AD")
      assertThat(offenders.map { it.probationPractitioner }).containsExactlyInAnyOrder("A Com", "Another Com")
      assertPostponeDetails(offenders, "A1234AB")
    }
  }

  private companion object {
    val govUkMockServer = GovUkMockServer()
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      govUkMockServer.start()
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      govUkMockServer.stop()
      prisonRegisterMockServer.stop()
    }
  }

  private fun assertPostponeDetails(offenders: List<OffenderSummary>, prisonNumber: String) {
    val summaryWithPostponement = offenders.firstOrNull { it.prisonNumber == prisonNumber }
    assertThat(summaryWithPostponement).isNotNull
    summaryWithPostponement?.let {
      assertThat(it.postponementDate).isEqualTo(LocalDate.of(2021, 12, 18))
      assertThat(it.postponementReasons)
        .containsExactly(
          PostponeCaseReasonType.PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER,
          PostponeCaseReasonType.ON_REMAND,
          PostponeCaseReasonType.SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE,
          PostponeCaseReasonType.NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION,
          PostponeCaseReasonType.COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY,
          PostponeCaseReasonType.CONFISCATION_ORDER_NOT_PAID_AND_ENF_AGENCY_DEEMS_UNSUITABLE,
          PostponeCaseReasonType.PENDING_APPLICATION_WITH_UNDULY_LENIENT_LENIENT_SCH,
        )
    }
  }
}
