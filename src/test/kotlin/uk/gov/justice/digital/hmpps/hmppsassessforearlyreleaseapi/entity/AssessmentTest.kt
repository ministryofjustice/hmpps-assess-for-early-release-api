package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
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
    val assessment = Assessment(offender = anOffender())

    assessment.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
    assessment.changeStatus(ELIGIBLE_AND_SUITABLE)

    val statusChangeEvents = assessment.assessmentEvents.map { it as StatusChangedEvent }.map { it.changes }

    assertThat(statusChangeEvents).containsExactly(
      StatusChange(before = NOT_STARTED, after = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS),
      StatusChange(before = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, after = ELIGIBLE_AND_SUITABLE),
    )
  }

  @Test
  fun `does not record status change when status has not changed`() {
    val assessment = Assessment(offender = anOffender(), status = ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

    assessment.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
    assessment.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
    assessment.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

    assertThat(assessment.assessmentEvents).isEmpty()
  }
}
