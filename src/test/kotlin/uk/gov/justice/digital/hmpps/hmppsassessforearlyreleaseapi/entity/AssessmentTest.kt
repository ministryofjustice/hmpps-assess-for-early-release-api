package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.EligibilityAndSuitabilityAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.OptBackIn
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.OptOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.answers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.criterionChecks

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
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(IN_PROGRESS, criterionChecks(CriteriaType.ELIGIBILITY)), agent)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(IN_PROGRESS, criterionChecks(CriteriaType.ELIGIBILITY)), agent)
    assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(ELIGIBLE, criterionChecks(CriteriaType.ELIGIBILITY)), agent)

    assertThat(assessment.status).isEqualTo(ELIGIBLE_AND_SUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    val agents = assessment.assessmentEvents.map { it.agent }
    assertThat(agents).containsOnly(agent)
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(before = NOT_STARTED, after = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, context = mapOf("eligibilityStatus" to IN_PROGRESS, "type" to CriteriaType.ELIGIBILITY, "code" to criterion.code, "answers" to answers)),
      StatusChange(before = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, after = ELIGIBLE_AND_SUITABLE, context = mapOf("eligibilityStatus" to ELIGIBLE, "type" to CriteriaType.ELIGIBILITY, "code" to criterion.code, "answers" to answers)),
    )
  }

  @Test
  fun `returns to previous state`() {
    val assessment = Assessment(offender = anOffender(), status = INELIGIBLE_OR_UNSUITABLE)
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")
    val reasonType = NO_REASON_GIVEN
    val otherDescription = "No reason given"

    assessment.performTransition(OptOut(reasonType, otherDescription), agent)

    assertThat(assessment.status).isEqualTo(OPTED_OUT)
    assertThat(assessment.previousStatus).isEqualTo(INELIGIBLE_OR_UNSUITABLE)

    assessment.performTransition(OptBackIn(assessment.offender.prisonNumber), agent)

    assertThat(assessment.status).isEqualTo(INELIGIBLE_OR_UNSUITABLE)
    assertThat(assessment.previousStatus).isEqualTo(OPTED_OUT)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).containsExactly(
      StatusChange(before = INELIGIBLE_OR_UNSUITABLE, after = OPTED_OUT, context = mapOf("reason" to reasonType, "otherDescription" to otherDescription)),
      StatusChange(before = OPTED_OUT, after = INELIGIBLE_OR_UNSUITABLE, context = mapOf("prisonNumber" to assessment.offender.prisonNumber)),
    )
  }

  @Test
  fun `handles error side effect`() {
    val assessment = Assessment(offender = anOffender(), status = NOT_STARTED)
    val agent = Agent("user", UserRole.PRISON_CA, "HPE")

    assertThatThrownBy { assessment.performTransition(EligibilityAndSuitabilityAnswerProvided(ELIGIBLE, criterionChecks(CriteriaType.ELIGIBILITY)), agent) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Unable to transition to Eligible from NOT_STARTED directly")

    assertThat(assessment.status).isEqualTo(NOT_STARTED)
    assertThat(assessment.previousStatus).isNull()

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }
    assertThat(statusChangeEvents).isEmpty()
  }
}
