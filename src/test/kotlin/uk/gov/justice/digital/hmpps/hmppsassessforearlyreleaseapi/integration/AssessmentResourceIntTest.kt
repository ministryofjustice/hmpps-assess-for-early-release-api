package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_DM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PROBATION_COM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.GenericChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.COMPLETE_14_DAY_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.COMPLETE_2_DAY_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.ManagedUsersMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.ProbationSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentContactsResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ContactResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.NonDisclosableInformation
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.OTHER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.PostponeCaseRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateVloAndPomConsultationRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.DAYS_TO_ADD
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Consumer

private const val PRISON_NUMBER = TestData.PRISON_NUMBER
private const val GET_CURRENT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment"
private const val GET_CURRENT_ASSESSMENT_CONTACTS_URL = "/offender/$PRISON_NUMBER/current-assessment/contacts"
private const val OPT_OUT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/opt-out"
private const val OPT_IN_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/opt-in"
private const val POSTPONE_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/postpone"
private const val SUBMIT_FOR_ADDRESS_CHECKS_URL = "/offender/$PRISON_NUMBER/current-assessment/submit-for-address-checks"
private const val SUBMIT_FOR_PRE_DECISION_CHECKS_URL = "/offender/$PRISON_NUMBER/current-assessment/submit-for-pre-decision-checks"
private const val RECORD_NON_DISCLOSABLE_INFORMATION_URL = "/offender/$PRISON_NUMBER/current-assessment/record-non-disclosable-information"
private const val UPDATE_VLO_AND_POM_CONSULTATION_URL = "/offender/$PRISON_NUMBER/current-assessment/vlo-and-pom-consultation"

class AssessmentResourceIntTest : SqsIntegrationTestBase() {

  private companion object {
    val prisonerSearchApiMockServer = PrisonerSearchMockServer()
    val prisonRegisterMockServer = PrisonRegisterMockServer()
    val probationSearchApiMockServer = ProbationSearchMockServer()
    val deliusMockServer = DeliusMockServer()
    val managedUsersMockServer = ManagedUsersMockServer()
    val prisonApiMockServer = PrisonApiMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonerSearchApiMockServer.start()
      prisonRegisterMockServer.start()
      probationSearchApiMockServer.start()
      deliusMockServer.start()
      managedUsersMockServer.start()
      prisonApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonerSearchApiMockServer.stop()
      prisonRegisterMockServer.stop()
      probationSearchApiMockServer.stop()
      deliusMockServer.stop()
      managedUsersMockServer.stop()
      prisonApiMockServer.stop()
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
      // Given
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = 10L,
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
          bookingId = 10L,
          forename = "FIRST-1",
          surname = "LAST-1",
          prisonNumber = PRISON_NUMBER,
          dateOfBirth = LocalDate.of(1978, 3, 20),
          hdced = LocalDate.now().plusDays(7),
          crd = LocalDate.of(2020, 11, 14),
          location = "Birmingham (HMP)",
          status = NOT_STARTED,
          addressChecksStatus = ResidentialChecksStatus.NOT_STARTED,
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
            PRISON_DM to emptyList(),
            PROBATION_COM to emptyList(),
          ),
          toDoEligibilityAndSuitabilityBy = LocalDate.now().plusDays(DAYS_TO_ADD),
          result = null,
          lastUpdateBy = "Kovar Noj",
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
          PostponeCaseReasonType.BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON,
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

      val assessments = testAssessmentRepository.findByOffenderPrisonNumberOrderById(PRISON_NUMBER)
      assertThat(assessments).hasSize(1)

      val updatedAssessment = assessments.first()
      assertThat(updatedAssessment.postponementDate).isToday()
      assertThat(updatedAssessment.postponementReasons).hasSize(2)

      assertThat(updatedAssessment.postponementReasons[0])
        .satisfies(
          Consumer {
            assertThat(it.id).isNotNegative()
            assertThat(it.assessment).isEqualTo(updatedAssessment)
            assertThat(it.reasonType).isEqualTo(PostponeCaseReasonType.ON_REMAND)
            assertThat(it.createdTimestamp).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))
          },
        )

      assertThat(updatedAssessment.postponementReasons[1])
        .satisfies(
          Consumer {
            assertThat(it.id).isNotNegative()
            assertThat(it.assessment).isEqualTo(updatedAssessment)
            assertThat(it.reasonType).isEqualTo(PostponeCaseReasonType.BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON)
            assertThat(it.createdTimestamp).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))
          },
        )

      assertLastUpdateByUser(updatedAssessment)
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

      assertLastUpdateByUser(assessments.last())
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
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .exchange()

      // Then
      result.expectStatus()
        .isNoContent

      val offender = offenderRepository.findByPrisonNumber(PRISON_NUMBER)
        ?: Assertions.fail("couldn't find offender with prison number: $PRISON_NUMBER")
      val assessments = testAssessmentRepository.findByOffender(offender)
      val updatedAssessment = assessments.first()

      assertThat(updatedAssessment.status).isEqualTo(ELIGIBLE_AND_SUITABLE)
      assertLastUpdateByUser(updatedAssessment)
    }
  }

  @Nested
  inner class SubmitForAddressChecks {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .headers(setAuthorisation(agent = PROBATION_COM_AGENT))
        .bodyValue(PRISON_CA_AGENT)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG"), agent = PRISON_CA_AGENT))
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
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = webTestClient.put()
        .uri(SUBMIT_FOR_ADDRESS_CHECKS_URL)
        .headers(setAuthorisation(roles = roles, agent = PRISON_CA_AGENT))
        .exchange()

      // Then
      result.expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
      assertLastUpdateByUser(updatedAssessment)
    }
  }

  @Nested
  inner class SubmitForPreDecisionChecks {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .headers(setAuthorisation(agent = PROBATION_COM_AGENT))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG"), agent = PROBATION_COM_AGENT))
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
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val results = webTestClient.put()
        .uri(SUBMIT_FOR_PRE_DECISION_CHECKS_URL)
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .exchange()

      // Then
      results.expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.status).isEqualTo(AWAITING_PRE_DECISION_CHECKS)
      assertLastUpdateByUser(updatedAssessment)
    }
  }

  @Nested
  inner class RecordNonDisclosableInformation {

    private val anNonDisclosableInformationWithNoReason = NonDisclosableInformation(
      hasNonDisclosableInformation = false,
      nonDisclosableInformation = null,
    )

    private val anNonDisclosableInformationWithReason = NonDisclosableInformation(
      hasNonDisclosableInformation = true,
      nonDisclosableInformation = "reason",
    )

    private val anNonDisclosableInformationWithNoReasonAndisNonDisclosableTrue = NonDisclosableInformation(
      hasNonDisclosableInformation = true,
      nonDisclosableInformation = null,
    )

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(RECORD_NON_DISCLOSABLE_INFORMATION_URL)
        .bodyValue(anNonDisclosableInformationWithNoReason)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(RECORD_NON_DISCLOSABLE_INFORMATION_URL)
        .headers(setAuthorisation(agent = PROBATION_COM_AGENT))
        .bodyValue(anNonDisclosableInformationWithNoReason)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(RECORD_NON_DISCLOSABLE_INFORMATION_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG"), agent = PROBATION_COM_AGENT))
        .bodyValue(anNonDisclosableInformationWithNoReason)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-address-checks-complete.sql",
    )
    @Test
    fun `should record non disclosable reason with null value`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = webTestClient.put()
        .uri(RECORD_NON_DISCLOSABLE_INFORMATION_URL)
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(anNonDisclosableInformationWithNoReason)
        .exchange()

      // Then
      result.expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.hasNonDisclosableInformation).isFalse()
      assertThat(updatedAssessment.nonDisclosableInformation).isNull()

      val assessmentEvents =
        assessmentEventRepository.findByAssessmentId(updatedAssessment.id).filterIsInstance<GenericChangedEvent>()
      assertThat(assessmentEvents).isNotEmpty
      assertThat(assessmentEvents).hasSize(1)

      val eventProduced = assessmentEvents.first()

      assertThat(eventProduced.eventType).isEqualTo(AssessmentEventType.NONDISCLOSURE_INFORMATION_ENTRY)
      assertThat(eventProduced.changes).isEqualTo(
        mapOf(
          "hasNonDisclosableInformation" to "false",
          "nonDisclosableInformation" to "null",
        ),
      )
      assertLastUpdateByUser(updatedAssessment)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-address-checks-complete.sql",
    )
    @Test
    fun `should record non disclosable reason with non null value`() {
      webTestClient.put()
        .uri(RECORD_NON_DISCLOSABLE_INFORMATION_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PROBATION_COM_AGENT))
        .bodyValue(anNonDisclosableInformationWithReason)
        .exchange()
        .expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.hasNonDisclosableInformation).isEqualTo(anNonDisclosableInformationWithReason.hasNonDisclosableInformation)
      assertThat(updatedAssessment.nonDisclosableInformation).isEqualTo(anNonDisclosableInformationWithReason.nonDisclosableInformation)

      val assessmentEvents =
        assessmentEventRepository.findByAssessmentId(updatedAssessment.id).filterIsInstance<GenericChangedEvent>()
      assertThat(assessmentEvents).isNotEmpty
      assertThat(assessmentEvents).hasSize(1)
      val eventProduced = assessmentEvents.first()
      assertThat(eventProduced.eventType).isEqualTo(AssessmentEventType.NONDISCLOSURE_INFORMATION_ENTRY)
      assertThat(eventProduced.changes).isEqualTo(
        mapOf(
          "hasNonDisclosableInformation" to "true",
          "nonDisclosableInformation" to "reason",
        ),
      )

      assertThat(updatedAssessment.lastUpdateByUserEvent).isNotNull
      assertThat(updatedAssessment.lastUpdateByUserEvent).isEqualTo(eventProduced)
      assertThat(updatedAssessment.lastUpdateByUserEvent!!.agent.role).isNotEqualTo(UserRole.SYSTEM)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-address-checks-complete.sql",
    )
    @Test
    fun `should throw validation error while trying to insert non disclosable reason with null value and hasNonDisclosableInformation true`() {
      webTestClient.put()
        .uri(RECORD_NON_DISCLOSABLE_INFORMATION_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PROBATION_COM_AGENT))
        .bodyValue(anNonDisclosableInformationWithNoReasonAndisNonDisclosableTrue)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody()
        .jsonPath("$.userMessage")
        .value(containsString("If hasNonDisclosableInformation is true, nonDisclosableInformation must not be null or empty"))
    }
  }

  @Nested
  inner class UpdateVloAndPomConsultation {
    private val anUpdateVloAndPomConsultationRequest = UpdateVloAndPomConsultationRequest(
      victimContactSchemeOptedIn = true,
      victimContactSchemeRequests = "Do not come within an the area around where I work",
      pomBehaviourInformation = "Behaviour in prison suggest they would be safe to release on HDC",
    )

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(UPDATE_VLO_AND_POM_CONSULTATION_URL)
        .bodyValue(anUpdateVloAndPomConsultationRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(UPDATE_VLO_AND_POM_CONSULTATION_URL)
        .headers(setAuthorisation())
        .bodyValue(anUpdateVloAndPomConsultationRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(UPDATE_VLO_AND_POM_CONSULTATION_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG"), agent = PROBATION_COM_AGENT))
        .bodyValue(anUpdateVloAndPomConsultationRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-address-checks-complete.sql",
    )
    @Test
    fun `should update VLO and POM consultation information`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When

      val result = webTestClient.put()
        .uri(UPDATE_VLO_AND_POM_CONSULTATION_URL)
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(anUpdateVloAndPomConsultationRequest)
        .exchange()

      // Then
      result.expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.victimContactSchemeOptedIn).isEqualTo(anUpdateVloAndPomConsultationRequest.victimContactSchemeOptedIn)
      assertThat(updatedAssessment.victimContactSchemeRequests).isEqualTo(anUpdateVloAndPomConsultationRequest.victimContactSchemeRequests)
      assertThat(updatedAssessment.pomBehaviourInformation).isEqualTo(anUpdateVloAndPomConsultationRequest.pomBehaviourInformation)
      assertLastUpdateByUser(updatedAssessment)
    }
  }

  @Nested
  inner class GetAssessmentContactDetails {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_CONTACTS_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_CONTACTS_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_CONTACTS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/assessment-with-event-history.sql",
    )
    @Test
    fun `should return the latest contacts for current assessment`() {
      // Given
      val usernameCA = "a-prison-user"

      prisonRegisterMockServer.stubGetPrisons()
      managedUsersMockServer.stubGetOffenderManager(usernameCA, email = "bura.hurn@moj.gov.uk")
      prisonApiMockServer.stubGetUserDetails(usernameCA, prisonId = "AKI")

      val usernameDM = "a-dm-user"

      prisonRegisterMockServer.stubGetPrisons()
      managedUsersMockServer.stubGetOffenderManager(usernameDM, email = "kreg.rahnaz@moj.gov.uk")
      prisonApiMockServer.stubGetUserDetails(usernameDM, prisonId = "BMI")

      val usernamePB = "a-probation-user"

      prisonRegisterMockServer.stubGetPrisons()
      deliusMockServer.stubGetStaffDetailsByUsername(usernamePB, email = "margon.antaak@moj.gov.uk")
      prisonApiMockServer.stubGetUserDetails(usernamePB, prisonId = "BMI")

      // When
      val result = webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_CONTACTS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()

      // Then
      result.expectStatus().isOk
      val assessmentContactsResponse =
        result.expectBody(AssessmentContactsResponse::class.java).returnResult().responseBody!!
      assertThat(assessmentContactsResponse).isNotNull
      assertThat(assessmentContactsResponse.contacts).size().isEqualTo(3)

      assertThat(assessmentContactsResponse.contacts).containsExactly(
        ContactResponse(
          fullName = "Margon Antaak",
          userRole = PROBATION_COM,
          email = "margon.antaak@moj.gov.uk",
          locationName = null,
        ),
        ContactResponse(
          fullName = "Kreg Rahnaz",
          userRole = PRISON_DM,
          email = "kreg.rahnaz@moj.gov.uk",
          locationName = "Birmingham (HMP)",
        ),
        ContactResponse(
          fullName = "Bura Hurn",
          userRole = PRISON_CA,
          email = "bura.hurn@moj.gov.uk",
          locationName = "Acklington (HMP)",
        ),
      )
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/assessment-with-event-history.sql",
    )
    @Test
    fun `should return the some contacts details even if external calls fail`() {
      // Given
      val usernameCA = "a-prison-user"

      prisonRegisterMockServer.stubGetPrisons()
      managedUsersMockServer.stubGetOffenderManager404(usernameCA)
      prisonApiMockServer.stubGetUserDetails404(usernameCA)

      val usernameDM = "a-dm-user"

      prisonRegisterMockServer.stubGetPrisons()
      managedUsersMockServer.stubGetOffenderManager404(usernameDM)
      prisonApiMockServer.stubGetUserDetails404(usernameDM)

      val usernamePB = "a-probation-user"

      prisonRegisterMockServer.stubGetPrisons()
      deliusMockServer.stubPostStaffDetailsByUsername404(usernamePB)
      prisonApiMockServer.stubGetUserDetails404(usernamePB)

      // When
      val result = webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_CONTACTS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()

      // Then
      result.expectStatus().isOk
      val assessmentContactsResponse =
        result.expectBody(AssessmentContactsResponse::class.java).returnResult().responseBody!!
      assertThat(assessmentContactsResponse).isNotNull
      assertThat(assessmentContactsResponse.contacts).size().isEqualTo(3)

      assertThat(assessmentContactsResponse.contacts).containsExactly(
        ContactResponse(fullName = "Margon Antaak", userRole = PROBATION_COM, email = null, locationName = null),
        ContactResponse(fullName = "Kreg Rahnaz", userRole = PRISON_DM, email = null, locationName = null),
        ContactResponse(fullName = "Bura Hurn", userRole = PRISON_CA, email = null, locationName = null),
      )
    }
  }

  private fun assertLastUpdateByUser(assessment: Assessment) {
    assertThat(assessment.lastUpdateByUserEvent).isNotNull
    assertThat(assessment.lastUpdateByUserEvent).isEqualTo(assessment.getEvents().first())
    assertThat(assessment.lastUpdateByUserEvent!!.agent.role).isNotEqualTo(UserRole.SYSTEM)
  }
}
