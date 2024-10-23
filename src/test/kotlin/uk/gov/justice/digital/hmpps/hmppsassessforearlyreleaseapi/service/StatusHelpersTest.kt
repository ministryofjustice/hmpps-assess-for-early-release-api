package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.AbstractComparableAssert
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anEligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anSuitabilityCheckDetails

class StatusHelpersTest {

  @Nested
  inner class CalculateAggregateStatus {

    @Test
    fun `no progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(NOT_STARTED)
    }

    @Test
    fun `suitability in progress`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = NOT_STARTED),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
      )

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
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

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `suitability and eligibility in-progress`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = NOT_STARTED),
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
      )
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
      )

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `unsuitable but not ineligible`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `ineligible without complete suitability`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE), anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `unsuitable without complete eligibility`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE), anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(IN_PROGRESS)
    }

    @Test
    fun `ineligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `eligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(ELIGIBLE)
    }

    @Test
    fun `mixed eligibility results`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
        anEligibilityCheckDetails().copy(status = INELIGIBLE),
      )
      val suitabilityDetails = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(INELIGIBLE)
    }

    @Test
    fun `outstanding suitability questions when eligible is incomplete unless unsuitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityDetails = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS),
      )

      assertThatOverallStatus(eligibilityDetails, suitabilityDetails).isEqualTo(INELIGIBLE)
    }

    private fun assertThatOverallStatus(
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
        ).calculateAggregateStatus(),
      )
  }
}
