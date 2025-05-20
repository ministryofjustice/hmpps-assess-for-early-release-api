package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.GovUkMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference
import java.time.LocalDate

private const val PRISON_CODE = "BMI"
private const val STAFF_CODE = "STAFF1"
private const val GET_CASE_ADMIN_CASELOAD_URL = "/prison/$PRISON_CODE/case-admin/caseload"
private const val GET_COM_STAFF_CASELOAD_URL = "/probation/community-offender-manager/staff-code/$STAFF_CODE/caseload"
private const val GET_COM_TEAM_CASELOAD_URL = "/probation/community-offender-manager/staff-code/$STAFF_CODE/team-caseload"
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
    fun `should return offenders at prison`() {
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

      val offenders = response.expectBody(typeReference<List<OffenderSummaryResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(4)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CD")
      assertThat(offenders.map { it.crn }).containsExactlyInAnyOrder("DX12340A", null, "DX12340D", "DX12340F")
      assertPostponeDetails(offenders, "A1234AA")
      assertThat(offenders.map { it.lastUpdateBy }).containsExactlyInAnyOrder("Kovar Noj", null, null, null)
    }
  }

  @Nested
  inner class GetComStaffCaseload {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_COM_STAFF_CASELOAD_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_COM_STAFF_CASELOAD_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_COM_STAFF_CASELOAD_URL)
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
        .uri(GET_COM_STAFF_CASELOAD_URL)
        .headers(setAuthorisation(roles = roles))
        .exchange()

      // Then
      response.expectStatus().isOk

      val offenders = response.expectBody(typeReference<List<OffenderSummaryResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(4)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AB", "G9524ZF", "C1234CC", "B1234BB")
      assertThat(offenders.map { it.crn }).containsExactlyInAnyOrder(null, "DX12340C", "DX12340E", "DX12340G")
      assertThat(offenders.map { it.probationPractitioner }).containsOnly("A Com")
      assertPostponeDetails(offenders, "A1234AB")
    }
  }

  @Nested
  inner class GetComTeamCaseload {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_COM_TEAM_CASELOAD_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_COM_TEAM_CASELOAD_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_COM_TEAM_CASELOAD_URL)
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
    fun `should return offenders allocated to a COM users teams`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      deliusMockServer.stubGetStaffDetailsByStaffCode(STAFF_CODE)

      // When
      val response = webTestClient.get()
        .uri(GET_COM_TEAM_CASELOAD_URL)
        .headers(setAuthorisation(roles = roles))
        .exchange()

      // Then
      response.expectStatus().isOk

      val offenders = response.expectBody(typeReference<List<OffenderSummaryResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(5)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CC", "C1234CD")
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

      val offenders = response.expectBody(typeReference<List<OffenderSummaryResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(4)
      assertThat(offenders.map { it.prisonNumber }).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CD")
      assertThat(offenders.map { it.probationPractitioner }).containsExactlyInAnyOrder("Another Com", "A Com", "Another Com", "Another Com")
      assertThat(offenders.map { it.crn }).containsExactlyInAnyOrder("DX12340A", "DX12340B", null, "DX12340F")

      assertPostponeDetails(offenders, "A1234AB")
    }
  }

  private companion object {
    val deliusMockServer = DeliusMockServer()
    val govUkMockServer = GovUkMockServer()
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      deliusMockServer.start()
      govUkMockServer.start()
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      deliusMockServer.stop()
      govUkMockServer.stop()
      prisonRegisterMockServer.stop()
    }
  }

  private fun assertPostponeDetails(offenders: List<OffenderSummaryResponse>, prisonNumber: String) {
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
          PostponeCaseReasonType.BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON,
          PostponeCaseReasonType.WOULD_NOT_FOLLOW_REQUIREMENTS_OF_CONFISCATION_ORDER,
          PostponeCaseReasonType.PENDING_APPLICATION_WITH_UNDULY_LENIENT_SENTENCE_SCH,
        )
    }
  }
}
