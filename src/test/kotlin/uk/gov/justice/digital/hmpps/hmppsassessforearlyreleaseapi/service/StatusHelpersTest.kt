package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.AbstractComparableAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getIneligibleReasons
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getUnsuitableReasons
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anEligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anSuitabilityCheckDetails

class StatusHelpersTest {

  @Nested
  inner class CalculateAggregateStatus {

    @Test
    fun `no progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(NOT_STARTED)
    }

    @Test
    fun `eligibility in progress`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = NOT_STARTED),
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
      )

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun ineligible() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
        anEligibilityCheckDetails().copy(status = INELIGIBLE),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
      )

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `eligible and suitability not started`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
      )

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `eligible and suitability in-progress`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
      )

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `eligible and unsuitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE),
      )

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `eligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
      )

      overallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(ELIGIBLE)
    }

    @Test
    fun `some progress on suitability without eligibility checks having started`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
      )

      assertThatThrownBy {
        overallStatus(
          eligibilityDetails,
          suitabilityDetails,
        )
      }.hasMessage("Should not be possible to start suitability without being eligible")
    }

    @Test
    fun `some progress on suitability whilst eligibility checks in progress`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = NOT_STARTED),
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
      )

      assertThatThrownBy {
        overallStatus(
          eligibilityDetails,
          suitabilityDetails,
        )
      }.hasMessage("Should not be possible to start suitability without being eligible")
    }

    private fun overallStatus(
      eligibilityDetails: List<EligibilityCriterionProgress>,
      suitabilityDetails: List<SuitabilityCriterionProgress>,
    ): AbstractComparableAssert<*, EligibilityStatus> =
      assertThat(
        AssessmentWithEligibilityProgress(
          offender = TestData.anOffender(),
          assessmentEntity = TestData.anOffender().currentAssessment(),
          prison = "Moorland",
          eligibilityProgress = { eligibilityDetails },
          suitabilityProgress = { suitabilityDetails },
        ).calculateAggregateEligibilityStatus(),
      )
  }

  @Nested
  inner class GetFailureReasons {

    @Test
    fun `ineligibility reasons`() {
      val reasons = listOf(
        anEligibilityCheckDetails().copy(status = INELIGIBLE, taskName = "Thing 1"),
        anEligibilityCheckDetails().copy(status = NOT_STARTED, taskName = "Thing 2"),
        anEligibilityCheckDetails().copy(status = INELIGIBLE, taskName = "Thing 3"),
        anEligibilityCheckDetails().copy(status = IN_PROGRESS, taskName = "Thing 4"),
      ).getIneligibleReasons()

      assertThat(reasons).containsExactly("Thing 1", "Thing 3")
    }

    @Test
    fun `unsuitability reasons`() {
      val reasons = listOf(
        anSuitabilityCheckDetails().copy(status = UNSUITABLE, taskName = "Thing 1"),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED, taskName = "Thing 2"),
        anSuitabilityCheckDetails().copy(status = UNSUITABLE, taskName = "Thing 3"),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS, taskName = "Thing 4"),
      ).getUnsuitableReasons()

      assertThat(reasons).containsExactly("Thing 1", "Thing 3")
    }
  }
}
