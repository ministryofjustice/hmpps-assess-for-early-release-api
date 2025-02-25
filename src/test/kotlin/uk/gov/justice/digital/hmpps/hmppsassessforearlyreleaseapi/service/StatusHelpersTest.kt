package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.AbstractComparableAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getIneligibleReasons
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getUnsuitableReasons
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ResultType.FAILED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ResultType.PASSED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anEligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anSuitabilityCheckDetails

class StatusHelpersTest {

  @Nested
  inner class CalculateAggregateStatus {

    @Test
    fun `no progress`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.NOT_STARTED,
        eligibilityProgress = Progress.none(),
        suitabilityProgress = Progress.none(),
      )
      overallStatus(assessment).isEqualTo(NOT_STARTED)
    }

    @Test
    fun `eligibility in progress`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.specifyByIndex(0 to PASSED),
        suitabilityProgress = Progress.none(),
      )
      overallStatus(assessment).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun ineligible() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.INELIGIBLE_OR_UNSUITABLE,
        eligibilityProgress = Progress.specifyByIndex(0 to FAILED),
        suitabilityProgress = Progress.none(),
      )

      overallStatus(assessment).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `eligible and suitability not started`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.allSuccessful(),
        suitabilityProgress = Progress.none(),
      )

      overallStatus(assessment).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `eligible and suitability in-progress`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.allSuccessful(),
        suitabilityProgress = Progress.specifyByIndex(0 to PASSED),
      )

      overallStatus(assessment).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `eligible and unsuitable`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.allSuccessful(),
        suitabilityProgress = Progress.specifyByIndex(0 to PASSED, 1 to FAILED),
      )

      overallStatus(assessment).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `eligible and suitable`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.allSuccessful(),
        suitabilityProgress = Progress.allSuccessful(),
      )

      overallStatus(assessment).isEqualTo(ELIGIBLE)
    }

    @Test
    fun `some progress on suitability without eligibility checks having started`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.none(),
        suitabilityProgress = Progress.specifyByIndex(0 to PASSED),
      )

      assertThatThrownBy {
        overallStatus(assessment)
      }.hasMessage("Should not be possible to start suitability without being eligible")
    }

    @Test
    fun `some progress on suitability whilst eligibility checks in progress`() {
      val assessment = TestData.anAssessmentWithSomeProgress(
        AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.specifyByIndex(1 to PASSED),
        suitabilityProgress = Progress.specifyByIndex(1 to PASSED),
      )

      assertThatThrownBy {
        overallStatus(assessment)
      }.hasMessage("Should not be possible to start suitability without being eligible")
    }

    private fun overallStatus(
      assessmentWithEligibilityProgress: AssessmentWithEligibilityProgress,
    ): AbstractComparableAssert<*, EligibilityStatus> = assertThat(assessmentWithEligibilityProgress.calculateAggregateEligibilityStatus())
  }

  @Nested
  inner class GetFailureReasons {

    @Test
    fun `ineligibility reasons`() {
      val reasons = listOf(
        anEligibilityCheckDetails(1).copy(status = INELIGIBLE, taskName = "Thing 1"),
        anEligibilityCheckDetails(2).copy(status = NOT_STARTED, taskName = "Thing 2"),
        anEligibilityCheckDetails(3).copy(status = INELIGIBLE, taskName = "Thing 3"),
        anEligibilityCheckDetails(4).copy(status = IN_PROGRESS, taskName = "Thing 4"),
      ).getIneligibleReasons()

      assertThat(reasons).containsExactly("Thing 1", "Thing 3")
    }

    @Test
    fun `unsuitability reasons`() {
      val reasons = listOf(
        anSuitabilityCheckDetails(1).copy(status = UNSUITABLE, taskName = "Thing 1"),
        anSuitabilityCheckDetails(2).copy(status = SuitabilityStatus.NOT_STARTED, taskName = "Thing 2"),
        anSuitabilityCheckDetails(3).copy(status = UNSUITABLE, taskName = "Thing 3"),
        anSuitabilityCheckDetails(4).copy(status = SuitabilityStatus.IN_PROGRESS, taskName = "Thing 4"),
      ).getUnsuitableReasons()

      assertThat(reasons).containsExactly("Thing 1", "Thing 3")
    }
  }
}
