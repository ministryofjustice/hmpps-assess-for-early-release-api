package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.EligibilityAndSuitabilityAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.OptBackIn
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.OptOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ADDRESS_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.APPROVED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_PRE_RELEASE_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_REFUSAL
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.Companion.toState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.PASSED_PRE_RELEASE_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.POSTPONED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.REFUSED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.RELEASED_ON_HDC
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.TIMED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CONFIRM_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.SEND_CHECKS_TO_PRISON
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.StatusChange
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.StatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toModel
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anPostponeCaseRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.answers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.criterion

class AssessmentTest {

  private val anAgent = Agent("mtynan", fullName = "Mark Tynan", UserRole.PRISON_CA, "HPE")
  private val anAgentEntity = anAgent

  @Test
  fun `add new eligibility criteria check`() {
    val assessment = Assessment(offender = anOffender())

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
    val assessment = Assessment(offender = anOffender())

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
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(IN_PROGRESS, CriteriaType.ELIGIBILITY, criterion.code, answers), anAgentEntity)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(IN_PROGRESS, CriteriaType.ELIGIBILITY, criterion.code, answers), anAgentEntity)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(ELIGIBLE, CriteriaType.ELIGIBILITY, criterion.code, answers), anAgentEntity)

    assertThat(assessment.status).isEqualTo(ELIGIBLE_AND_SUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    val agents = assessment.assessmentEvents.map { it.agent }
    assertThat(agents).containsOnly(anAgentEntity)
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(
        before = NOT_STARTED,
        after = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        context = mapOf(
          "eligibilityStatus" to IN_PROGRESS,
          "type" to CriteriaType.ELIGIBILITY,
          "code" to criterion.code,
          "answers" to answers,
        ),
      ),
      StatusChange(
        before = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        after = ELIGIBLE_AND_SUITABLE,
        context = mapOf(
          "eligibilityStatus" to ELIGIBLE,
          "type" to CriteriaType.ELIGIBILITY,
          "code" to criterion.code,
          "answers" to answers,
        ),
      ),
    )
  }

  @Test
  fun `returns to previous state`() {
    val assessment = Assessment(offender = anOffender(), status = INELIGIBLE_OR_UNSUITABLE)
    val reasonType = NO_REASON_GIVEN
    val otherDescription = "No reason given"

    assessment.performTransition(OptOut(reasonType, otherDescription), anAgentEntity)

    assertThat(assessment.status).isEqualTo(OPTED_OUT)
    assertThat(assessment.previousStatus).isEqualTo(INELIGIBLE_OR_UNSUITABLE)

    assessment.performTransition(OptBackIn, anAgentEntity)

    assertThat(assessment.status).isEqualTo(INELIGIBLE_OR_UNSUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(OPTED_OUT)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
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
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED)

    assertThatThrownBy { assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(ELIGIBLE, CriteriaType.ELIGIBILITY, criterion.code, answers), anAgentEntity) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Unable to transition to Eligible from NOT_STARTED directly")

    assertThat(assessment.status).isEqualTo(NOT_STARTED)
    assertThat(assessment.previousStatus).isNull()

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
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
    val assessment = Assessment(offender = anOffender(), status = fromState)

    // When
    assessment.performTransition(AssessmentLifecycleEvent.Postpone(anPostponeCaseRequest.reasonTypes), anAgentEntity)

    // Then
    assertThat(assessment.status).isEqualTo(POSTPONED)
    assertThat(assessment.previousStatus).isEqualTo(fromState)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
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
    val assessment = Assessment(offender = anOffender(), status = fromState)

    // When
    val result = assertThatThrownBy { assessment.performTransition(AssessmentLifecycleEvent.Postpone(anPostponeCaseRequest.reasonTypes), anAgentEntity) }

    // Then
    result.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Fail to transition Assessment: '-1', triggered by 'Postpone' from '${fromState.toState(fromState).javaClass.simpleName}'")
    assertThat(assessment.status).isEqualTo(fromState)
    assertThat(assessment.previousStatus).isNull()

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).isEmpty()
  }

  @Test
  fun `should get the current task based on the assessment state`() {
    val offender = anOffender()
    assertThat(anAssessment(offender, NOT_STARTED).currentTask()).isEqualTo(ASSESS_ELIGIBILITY)
    assertThat(anAssessment(offender, ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS).currentTask()).isEqualTo(ASSESS_ELIGIBILITY)
    assertThat(anAssessment(offender, ELIGIBLE_AND_SUITABLE).currentTask()).isEqualTo(ENTER_CURFEW_ADDRESS)
    assertThat(anAssessment(offender, INELIGIBLE_OR_UNSUITABLE).currentTask()).isNull()
    assertThat(anAssessment(offender, AWAITING_ADDRESS_AND_RISK_CHECKS).currentTask()).isEqualTo(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION)
    assertThat(anAssessment(offender, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS).currentTask()).isEqualTo(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION)
    assertThat(anAssessment(offender, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS).copy(addressChecksComplete = true).currentTask()).isEqualTo(SEND_CHECKS_TO_PRISON)
    assertThat(anAssessment(offender, ADDRESS_UNSUITABLE).currentTask()).isNull()
    assertThat(anAssessment(offender, AWAITING_PRE_DECISION_CHECKS).currentTask()).isEqualTo(REVIEW_APPLICATION_AND_SEND_FOR_DECISION)
    assertThat(anAssessment(offender, AWAITING_DECISION).currentTask()).isEqualTo(CONFIRM_RELEASE)
    assertThat(anAssessment(offender, AWAITING_REFUSAL).currentTask()).isEqualTo(CONFIRM_RELEASE)
    assertThat(anAssessment(offender, REFUSED).currentTask()).isNull()
    assertThat(anAssessment(offender, APPROVED).currentTask()).isEqualTo(Task.APPROVE_LICENCE)
    assertThat(anAssessment(offender, AWAITING_PRE_RELEASE_CHECKS).currentTask()).isNull()
    assertThat(anAssessment(offender, PASSED_PRE_RELEASE_CHECKS).currentTask()).isNull()
    assertThat(anAssessment(offender, TIMED_OUT).currentTask()).isNull()
    assertThat(anAssessment(offender, POSTPONED).currentTask()).isNull()
    assertThat(anAssessment(offender, OPTED_OUT).currentTask()).isEqualTo(ENTER_CURFEW_ADDRESS)
    assertThat(anAssessment(offender, RELEASED_ON_HDC).currentTask()).isNull()
  }
}
