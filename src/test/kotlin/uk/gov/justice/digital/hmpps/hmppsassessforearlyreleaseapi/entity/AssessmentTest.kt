package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.StatusChange
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.StatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityChecksPassed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.OptBackIn
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.OptOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.Postpone
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.ResidentialCheckAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.SubmitForAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ADDRESS_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.APPROVED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_RELEASE_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_REFUSAL
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.Companion.toState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.PASSED_PRE_RELEASE_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.POSTPONED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.REFUSED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.RELEASED_ON_HDC
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.TIMED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.APPROVE_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.CONFIRM_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.CONSULT_THE_VLO_AND_POM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.RECORD_NON_DISCLOSABLE_INFORMATION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toModel
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anPostponeCaseRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.answers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import java.time.LocalDate

class AssessmentTest {

  private val anAgent = Agent("kerla.abarsha", fullName = "Kerla Abarsha", UserRole.PRISON_CA, "HPE")
  private val anAgentEntity = anAgent
  private val hdced = LocalDate.now().plusDays(14)
  private val crd = LocalDate.now().plusDays(31)
  private val sentenceStartDate = LocalDate.now().plusDays(1)

  @Test
  fun `add new eligibility criteria check`() {
    val assessment = Assessment(offender = anOffender(), bookingId = BOOKING_ID, hdced = hdced)

    assessment.addOrReplaceEligibilityCriterionResult(
      ELIGIBILITY,
      "code-1",
      criterionMet = true,
      answers = mapOf("code-0" to true),
      agent = anAgent.toModel(),
    )
    assertThat(assessment.eligibilityCheckResults).hasSize(1)

    assertThat(assessment.eligibilityCheckResults.first())
      .usingRecursiveComparison().ignoringFields("assessment", "createdTimestamp", "lastUpdatedTimestamp")
      .isEqualTo(
        EligibilityCheckResult(
          id = -1L,
          assessment = assessment,
          criterionCode = "code-1",
          criterionType = ELIGIBILITY,
          criterionVersion = assessment.policyVersion,
          criterionMet = true,
          questionAnswers = mapOf("code-0" to true),
          agent = anAgentEntity,
        ),
      )
  }

  @Test
  fun `update existing eligibility criteria check`() {
    val assessment = Assessment(offender = anOffender(), bookingId = BOOKING_ID, hdced = hdced)

    assessment.addOrReplaceEligibilityCriterionResult(
      ELIGIBILITY,
      "code-1",
      criterionMet = true,
      answers = mapOf("code-0" to true),
      agent = anAgent.toModel(),
    )

    assessment.addOrReplaceEligibilityCriterionResult(
      ELIGIBILITY,
      "code-1",
      criterionMet = true,
      answers = mapOf("code-0" to false),
      agent = anAgent.toModel(),
    )

    assertThat(assessment.eligibilityCheckResults).hasSize(1)

    assertThat(assessment.eligibilityCheckResults.first())
      .usingRecursiveComparison().ignoringFields("assessment", "createdTimestamp", "lastUpdatedTimestamp")
      .isEqualTo(
        EligibilityCheckResult(
          id = -1L,
          assessment = assessment,
          criterionCode = "code-1",
          criterionType = ELIGIBILITY,
          criterionVersion = assessment.policyVersion,
          criterionMet = true,
          questionAnswers = mapOf("code-0" to false),
          agent = anAgentEntity,
        ),
      )
  }

  @Test
  fun `records status changes`() {
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED, bookingId = BOOKING_ID, hdced = hdced)
    assessment.performTransition(
      EligibilityAnswerProvided(CriteriaType.ELIGIBILITY, criterion.code, answers),
      anAgentEntity,
    )
    assessment.performTransition(
      EligibilityAnswerProvided(CriteriaType.ELIGIBILITY, criterion.code, answers),
      anAgentEntity,
    )
    assessment.performTransition(
      EligibilityChecksPassed(CriteriaType.ELIGIBILITY, criterion.code, answers),
      anAgentEntity,
    )

    assertThat(assessment.status).isEqualTo(ELIGIBLE_AND_SUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

    val statusChangeEvents = assessment.getEvents().map { it as StatusChangedEvent }.map { it.changes }
    val agents = assessment.getEvents().map { it.agent }
    assertThat(agents).containsOnly(anAgentEntity)
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(
        before = NOT_STARTED,
        after = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        context = mapOf(
          "type" to CriteriaType.ELIGIBILITY,
          "code" to criterion.code,
          "answers" to answers,
        ),
      ),
      StatusChange(
        before = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        after = ELIGIBLE_AND_SUITABLE,
        context = mapOf(
          "type" to CriteriaType.ELIGIBILITY,
          "code" to criterion.code,
          "answers" to answers,
        ),
      ),
    )
  }

  @Test
  fun `returns to previous state`() {
    val assessment =
      Assessment(offender = anOffender(), status = INELIGIBLE_OR_UNSUITABLE, bookingId = BOOKING_ID, hdced = hdced)
    val reasonType = NO_REASON_GIVEN
    val otherDescription = "No reason given"

    assessment.performTransition(OptOut(reasonType, otherDescription), anAgentEntity)

    assertThat(assessment.status).isEqualTo(OPTED_OUT)
    assertThat(assessment.previousStatus).isEqualTo(INELIGIBLE_OR_UNSUITABLE)

    assessment.performTransition(OptBackIn, anAgentEntity)

    assertThat(assessment.status).isEqualTo(INELIGIBLE_OR_UNSUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(OPTED_OUT)

    val statusChangeEvents = assessment.getEvents().map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(
        before = INELIGIBLE_OR_UNSUITABLE,
        after = OPTED_OUT,
        context = mapOf("reason" to reasonType, "otherDescription" to otherDescription),
      ),
      StatusChange(before = OPTED_OUT, after = INELIGIBLE_OR_UNSUITABLE, context = emptyMap()),
    )
  }

  @Test
  fun `handles error side effect`() {
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED, bookingId = BOOKING_ID, hdced = hdced)

    assertThatThrownBy {
      assessment.performTransition(
        EligibilityChecksPassed(
          CriteriaType.ELIGIBILITY,
          criterion.code,
          answers,
        ),
        anAgentEntity,
      )
    }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Unable to transition to Eligible from NOT_STARTED directly")

    assertThat(assessment.status).isEqualTo(NOT_STARTED)
    assertThat(assessment.previousStatus).isNull()

    val statusChangeEvents = assessment.getEvents().map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).isEmpty()
  }

  @ParameterizedTest(name = "Performs transition from value: {0} to POSTPONED")
  @EnumSource(
    names = [
      "AWAITING_PRE_DECISION_CHECKS", "AWAITING_DECISION", "APPROVED",
      "AWAITING_PRE_RELEASE_CHECKS", "PASSED_PRE_RELEASE_CHECKS",
    ],
  )
  fun `handles valid from states to postpone transition`(fromState: AssessmentStatus) {
    // Given
    val assessment = Assessment(offender = anOffender(), status = fromState, bookingId = BOOKING_ID, hdced = hdced)

    // When
    assessment.performTransition(Postpone(anPostponeCaseRequest.reasonTypes), anAgentEntity)

    // Then
    assertThat(assessment.status).isEqualTo(POSTPONED)
    assertThat(assessment.previousStatus).isEqualTo(fromState)

    val statusChangeEvents = assessment.getEvents().map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(
        before = fromState,
        after = POSTPONED,
        context = mapOf("reasonTypes" to anPostponeCaseRequest.reasonTypes),
      ),
    )
  }

  @ParameterizedTest(name = "Does not perform transition from value: {0} to POSTPONED")
  @EnumSource(
    names = [
      "NOT_STARTED", "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "ELIGIBLE_AND_SUITABLE",
      "AWAITING_ADDRESS_AND_RISK_CHECKS", "ADDRESS_AND_RISK_CHECKS_IN_PROGRESS",
      "ADDRESS_UNSUITABLE", "AWAITING_REFUSAL", "INELIGIBLE_OR_UNSUITABLE", "REFUSED", "TIMED_OUT",
      "POSTPONED", "RELEASED_ON_HDC",
    ],
  )
  fun `handles in-valid from states to postpone transition`(fromState: AssessmentStatus) {
    // Given
    val assessment = Assessment(offender = anOffender(), status = fromState, bookingId = BOOKING_ID, hdced = hdced)

    // When
    val result =
      assertThatThrownBy { assessment.performTransition(Postpone(anPostponeCaseRequest.reasonTypes), anAgentEntity) }

    // Then
    result.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Fail to transition Assessment: '-1', triggered by 'Postpone' from '${fromState.toState(fromState).javaClass.simpleName}'")
    assertThat(assessment.status).isEqualTo(fromState)
    assertThat(assessment.previousStatus).isNull()

    val statusChangeEvents = assessment.getEvents().map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).isEmpty()
  }

  @Test
  fun `should get the current task based on the assessment state`() {
    assertThat(
      assessment(NOT_STARTED, eligibilityStatus = EligibilityStatus.NOT_STARTED).currentTask(),
    ).isEqualTo(ASSESS_ELIGIBILITY)

    assertThat(
      assessment(
        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityStatus = EligibilityStatus.IN_PROGRESS,
      ).currentTask(),
    ).isEqualTo(ASSESS_ELIGIBILITY)

    assertThat(assessment(ELIGIBLE_AND_SUITABLE).currentTask()).isEqualTo(ENTER_CURFEW_ADDRESS)

    assertThat(
      assessment(INELIGIBLE_OR_UNSUITABLE, eligibilityStatus = EligibilityStatus.INELIGIBLE).currentTask(),
    ).isNull()

    assertThat(assessment(AWAITING_ADDRESS_AND_RISK_CHECKS).currentTask()).isEqualTo(CONSULT_THE_VLO_AND_POM)

    assertThat(
      assessment(AWAITING_ADDRESS_AND_RISK_CHECKS).copy(victimContactSchemeOptedIn = true).currentTask(),
    ).isEqualTo(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION)

    assertThat(
      assessment(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS).currentTask(),
    ).isEqualTo(RECORD_NON_DISCLOSABLE_INFORMATION)

    assertThat(
      assessment(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS).copy(addressChecksComplete = true).currentTask(),
    ).isEqualTo(RECORD_NON_DISCLOSABLE_INFORMATION)

    assertThat(assessment(ADDRESS_UNSUITABLE).currentTask()).isNull()

    assertThat(
      assessment(AWAITING_PRE_DECISION_CHECKS).currentTask(),
    ).isEqualTo(REVIEW_APPLICATION_AND_SEND_FOR_DECISION)

    assertThat(assessment(AWAITING_DECISION).currentTask()).isEqualTo(CONFIRM_RELEASE)

    assertThat(assessment(AWAITING_REFUSAL).currentTask()).isEqualTo(CONFIRM_RELEASE)

    assertThat(assessment(REFUSED).currentTask()).isNull()

    assertThat(assessment(APPROVED).currentTask()).isEqualTo(APPROVE_LICENCE)

    assertThat(assessment(AWAITING_PRE_RELEASE_CHECKS).currentTask()).isNull()

    assertThat(assessment(PASSED_PRE_RELEASE_CHECKS).currentTask()).isNull()

    assertThat(assessment(TIMED_OUT).currentTask()).isNull()

    assertThat(assessment(POSTPONED).currentTask()).isNull()

    assertThat(assessment(OPTED_OUT).currentTask()).isEqualTo(ENTER_CURFEW_ADDRESS)

    assertThat(assessment(RELEASED_ON_HDC).currentTask()).isNull()
  }

  fun assessment(status: AssessmentStatus, eligibilityStatus: EligibilityStatus = EligibilityStatus.ELIGIBLE) = anAssessment(anOffender(), hdced, crd, sentenceStartDate, status, eligibilityStatus = eligibilityStatus)

  @Test
  fun `happy path`() {
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED, bookingId = BOOKING_ID, hdced = hdced)
    fun perform(event: AssessmentLifecycleEvent) = assessment.performTransition(
      event,
      anAgentEntity,
    ).status

    with(assessment) {
      assertThat(status).isEqualTo(NOT_STARTED)

      // Start eligibility checks
      perform(EligibilityAnswerProvided(CriteriaType.ELIGIBILITY, criterion.code, answers))

      assertThat(status).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

      // Complete eligibility checks
      perform(EligibilityChecksPassed(CriteriaType.ELIGIBILITY, criterion.code, answers))

      assertThat(status).isEqualTo(ELIGIBLE_AND_SUITABLE)

      perform(SubmitForAddressChecks)
      assertThat(status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)

      // submit twice is ok
      perform(SubmitForAddressChecks)
      assertThat(status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)

      perform(
        ResidentialCheckAnswerProvided(ResidentialChecksStatus.IN_PROGRESS, criterion.code, answers),
      )
      assertThat(status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    }
  }
}
