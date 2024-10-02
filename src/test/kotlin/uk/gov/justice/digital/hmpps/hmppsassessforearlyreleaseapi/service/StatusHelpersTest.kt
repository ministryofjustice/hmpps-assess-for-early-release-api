package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anEligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anSuitabilityCheckDetails

class StatusHelpersTest {

  @Nested
  inner class IsComplete {
    @Test
    fun `no progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `some in progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `all in-progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `unsuitable but not ineligible`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `ineligible without complete suitability`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun `ineligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun `eligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun `some but not all eligbility criteria complete`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
        anEligibilityCheckDetails().copy(status = IN_PROGRESS),
      )
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `mixed eligibility results`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
        anEligibilityCheckDetails().copy(status = INELIGIBLE),
      )
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun `outstanding suitability questions when eligible is incomplete`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityStatus = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS),
      )

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `outstanding suitability questions when eligible is incomplete unless unsuitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityStatus = listOf(
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE),
        anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS),
      )

      assertThat(StatusHelpers.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }
  }

  @Nested
  inner class IsChecksPassed {
    @Test
    fun `no progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `some in progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `all in progress`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `unsuitable but not ineligible`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `ineligible without complete suitability`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `ineligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `eligible and suitable`() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun `some but not all eligible and suitable`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
        anEligibilityCheckDetails().copy(status = IN_PROGRESS),
      )
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun `mixed eligible and in progress suitable`() {
      val eligibilityDetails = listOf(
        anEligibilityCheckDetails().copy(status = ELIGIBLE),
        anEligibilityCheckDetails().copy(status = INELIGIBLE),
      )
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(StatusHelpers.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }
  }
}
