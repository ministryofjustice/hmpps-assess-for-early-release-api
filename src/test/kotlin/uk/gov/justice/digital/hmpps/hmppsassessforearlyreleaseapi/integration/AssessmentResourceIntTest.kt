package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.COMPLETE_14_DAY_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.COMPLETE_2_DAY_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.OTHER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.PostponeCaseRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.DAYS_TO_ADD
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.function.Consumer

private const val PRISON_NUMBER = TestData.PRISON_NUMBER
private const val GET_CURRENT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment"
private const val DELETE_CURRENT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment"
private const val OPT_OUT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/opt-out"
private const val OPT_IN_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/opt-in"
private const val POSTPONE_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/postpone"
private const val SUBMIT_FOR_ADDRESS_CHECKS_URL = "/offender/$PRISON_NUMBER/current-assessment/submit-for-address-checks"
private const val SUBMIT_FOR_PRE_DECISION_CHECKS_URL = "/offender/$PRISON_NUMBER/current-assessment/submit-for-pre-decision-checks"

class AssessmentResourceIntTest : SqsIntegrationTestBase() {

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
      // Given
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = "123",
              prisonerNumber = PRISON_NUMBER,
              prisonId = "HMI",
              firstName = "FIRST-1",
              lastName = "LAST-1",
              dateOfBirth = LocalDate.of(1981, 5, 23),
              homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(7),
              cellLocation = "A-1-002",
              mostSeriousOffence = "Robbery",
              prisonName = "Birmingham (HMP)",
            ),
          ),
        ),
      )

      // When
      val result = webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()

      // Then
      result.expectStatus().isOk
      val assessment = result.expectBody(AssessmentOverviewSummary::class.java).returnResult().responseBody!!
      assertThat(assessment).isEqualTo(
        AssessmentOverviewSummary(
          forename = "FIRST-1",
          surname = "LAST-1",
          prisonNumber = PRISON_NUMBER,
          dateOfBirth = LocalDate.of(1978, 3, 20),
          hdced = LocalDate.now().plusDays(7),
          crd = LocalDate.of(2020, 11, 14),
          location = "Birmingham (HMP)",
          status = NOT_STARTED,
          policyVersion = "1.0",
          optOutReasonType = OTHER,
          optOutReasonOther = "I have reason",
          cellLocation = "A-1-002",
          mainOffense = "Robbery",
          tasks = mapOf(
            PRISON_CA to listOf(
              TaskProgress(name = ASSESS_ELIGIBILITY, progress = READY_TO_START),
              TaskProgress(name = ENTER_CURFEW_ADDRESS, progress = LOCKED),
              TaskProgress(name = REVIEW_APPLICATION_AND_SEND_FOR_DECISION, progress = LOCKED),
              TaskProgress(name = COMPLETE_14_DAY_CHECKS, progress = LOCKED),
              TaskProgress(name = COMPLETE_2_DAY_CHECKS, progress = LOCKED),
              TaskProgress(name = PRINT_LICENCE, progress = LOCKED),
            ),
          ),
          toDoEligibilityAndSuitabilityBy = LocalDate.now().plusDays(DAYS_TO_ADD),
          result = null,
        ),
      )
    }
  }

  @Nested
  open inner class PostponeCase {

    private inner class PostponeCaseRequestNoAgent(
      val reasonTypes: LinkedHashSet<PostponeCaseReasonType> = LinkedHashSet(),
    )

    private val anPostponeCaseRequestNoAgent = PostponeCaseRequestNoAgent(
      reasonTypes = LinkedHashSet(listOf(PostponeCaseReasonType.ON_REMAND)),
    )

    private val anPostponeCaseRequestWithNoReason = PostponeCaseRequest(
      reasonTypes = LinkedHashSet(),
      agent = PRISON_CA_AGENT,
    )

    private val anPostponeCaseRequest = PostponeCaseRequest(
      reasonTypes =
      LinkedHashSet(
        listOf(
          PostponeCaseReasonType.ON_REMAND,
          PostponeCaseReasonType.COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY,
        ),
      ),
      agent = PRISON_CA_AGENT,
    )

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(POSTPONE_ASSESSMENT_URL)
        .bodyValue(anPostponeCaseRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(POSTPONE_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .bodyValue(anPostponeCaseRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(POSTPONE_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(anPostponeCaseRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-passed-pre-release-checks-for-postponement.sql",
    )
    @Test
    fun `should postpone an offenders assessment`() {
      // Given
      val request = anPostponeCaseRequest.copy()
      val headers = setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"))

      // When
      val result = webTestClient.put()
        .uri(POSTPONE_ASSESSMENT_URL)
        .headers(headers)
        .bodyValue(request)
        .exchange()

      // Then
      result.expectStatus().isNoContent

      val assessments = testAssessmentRepository.findByOffenderPrisonNumber(PRISON_NUMBER)
      assertThat(assessments).hasSize(1)

      val assessment = assessments.first()
      assertThat(assessment.postponementDate).isToday()
      assertThat(assessment.postponementReasons).hasSize(2)

      assertThat(assessment.postponementReasons[0])
        .satisfies(
          Consumer {
            assertThat(it.id).isNotNegative()
            assertThat(it.assessment).isEqualTo(assessment)
            assertThat(it.reasonType).isEqualTo(PostponeCaseReasonType.ON_REMAND)
            assertThat(it.createdTimestamp).isCloseToUtcNow(within(2, ChronoUnit.SECONDS))
          },
        )

      assertThat(assessment.postponementReasons[1])
        .satisfies(
          Consumer {
            assertThat(it.id).isNotNegative()
            assertThat(it.assessment).isEqualTo(assessment)
            assertThat(it.reasonType).isEqualTo(PostponeCaseReasonType.COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY)
            assertThat(it.createdTimestamp).isCloseToUtcNow(within(2, ChronoUnit.SECONDS))
          },
        )
    }

    @Test
    fun `should throw bad request error if agent not given`() {
      // Given
      val headers = setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"))

      // When
      val result = webTestClient.put()
        .uri(POSTPONE_ASSESSMENT_URL)
        .headers(headers)
        .bodyValue(anPostponeCaseRequestNoAgent)
        .exchange()

      // Then
      result.expectStatus().isBadRequest
    }

    @Test
    fun `should throw bad request error if no reason given`() {
      // Given
      val headers = setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"))

      // When
      val result = webTestClient.put()
        .uri(POSTPONE_ASSESSMENT_URL)
        .headers(headers)
        .bodyValue(anPostponeCaseRequestWithNoReason)
        .exchange()

      // Then
      result.expectStatus().isBadRequest
    }
  }

  @Nested
  inner class OptOut {
    private val anOptOutRequest = OptOutRequest(reasonType = NO_REASON_GIVEN, agent = PRISON_CA_AGENT)

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .bodyValue(anOptOutRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .bodyValue(anOptOutRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(anOptOutRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-eligible-and-suitable-offender.sql",
    )
    @Test
    fun `should opt-out an offender`() {
      // Given
      val request = anOptOutRequest.copy(reasonType = OTHER, otherDescription = "an opt-out reason")
      val headers = setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"))

      // When
      val result = webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(headers)
        .bodyValue(request)
        .exchange()

      // Then
      result.expectStatus().isNoContent

      val offender = offenderRepository.findByPrisonNumber(PRISON_NUMBER)
        ?: Assertions.fail("couldn't find offender with prison number: $PRISON_NUMBER")
      val assessments = testAssessmentRepository.findByOffender(offender)
      assertThat(assessments)
        .hasSize(1)
        .extracting(Assessment::status, Assessment::optOutReasonType, Assessment::optOutReasonOther)
        .containsOnly(tuple(OPTED_OUT, OTHER, "an opt-out reason"))
    }

    @Test
    fun `should return 401 if otherDescription not given when reasonType is other`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(anOptOutRequest.copy(reasonType = OTHER))
        .exchange()
        .expectStatus()
        .isBadRequest
    }
  }

  @Nested
  inner class OptIn {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-opted-out-offender.sql",
    )
    @Test
    fun `should opt-in an offender`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isNoContent

      val offender = offenderRepository.findByPrisonNumber(PRISON_NUMBER)
        ?: Assertions.fail("couldn't find offender with prison number: $PRISON_NUMBER")
      val assessment = testAssessmentRepository.findByOffender(offender)
      assertThat(assessment.first().status).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }
  }

  @Nested
  inner class SubmitForAddressChecks {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .headers(setAuthorisation())
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-eligible-and-suitable-offender.sql",
    )
    @Test
    fun `should submit an assessment for address checks`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
    }
  }

  @Nested
  inner class SubmitForPreDecisionChecks {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .bodyValue(PROBATION_COM_AGENT)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .headers(setAuthorisation())
        .bodyValue(PROBATION_COM_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(PROBATION_COM_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-address-checks-complete.sql",
    )
    @Test
    fun `should submit an assessment for pre-decision checks`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(PROBATION_COM_AGENT)
        .exchange()
        .expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.status).isEqualTo(AWAITING_PRE_DECISION_CHECKS)
    }
  }

  private companion object {
    val prisonerSearchApiMockServer = PrisonerSearchMockServer()
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonerSearchApiMockServer.start()
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonerSearchApiMockServer.stop()
      prisonRegisterMockServer.stop()
    }
  }
}
