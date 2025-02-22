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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender

class AssessmentTest {

  @Test
  fun `add new eligibility criteria check`() {
    val assessment = Assessment(offender = anOffender())

    assessment.addOrReplaceEligibilityCriterionResult(
      ELIGIBILITY,
      "code-1",
      criterionMet = true,
      answers = mapOf("code-0" to true),
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
    )

    assessment.addOrReplaceEligibilityCriterionResult(
      ELIGIBILITY,
      "code-1",
      criterionMet = true,
      answers = mapOf("code-0" to false),
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
        ),
      )
  }

  @Test
  fun `records status changes`() {
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED)
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(IN_PROGRESS), agent)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(IN_PROGRESS), agent)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(ELIGIBLE), agent)

    assertThat(assessment.status).isEqualTo(ELIGIBLE_AND_SUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    val agents = assessment.assessmentEvents.map { it.agent }
    assertThat(agents).containsOnly(agent)
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(before = NOT_STARTED, after = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS),
      StatusChange(before = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, after = ELIGIBLE_AND_SUITABLE),
    )
  }

  @Test
  fun `returns to previous state`() {
    val assessment = Assessment(offender = anOffender(), status = INELIGIBLE_OR_UNSUITABLE)
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")

    assessment.performTransition(OptOut, agent)

    assertThat(assessment.status).isEqualTo(OPTED_OUT)
    assertThat(assessment.previousStatus).isEqualTo(INELIGIBLE_OR_UNSUITABLE)

    assessment.performTransition(OptBackIn, agent)

    assertThat(assessment.status).isEqualTo(INELIGIBLE_OR_UNSUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(OPTED_OUT)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(before = INELIGIBLE_OR_UNSUITABLE, after = OPTED_OUT),
      StatusChange(before = OPTED_OUT, after = INELIGIBLE_OR_UNSUITABLE),
    )
  }

  @Test
  fun `handles error side effect`() {
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED)
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")

    assertThatThrownBy { assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(ELIGIBLE), agent) }
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
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")

    // When
    assessment.performTransition(AssessmentLifecycleEvent.Postpone, agent)

    // Then
    assertThat(assessment.status).isEqualTo(POSTPONED)
    assertThat(assessment.previousStatus).isEqualTo(fromState)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(before = fromState, after = POSTPONED),
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
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")

    // When
    val result = assertThatThrownBy { assessment.performTransition(AssessmentLifecycleEvent.Postpone, agent) }

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
