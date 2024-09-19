package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anEligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anSuitabilityCheckDetails

class InitialChecksServiceTest {
  private val offenderService = mock<OffenderService>()
  private val service = InitialChecksService(offenderService, PolicyService())

  @Test
  fun `getCurrentInitialChecks for existing unstarted offender`() {
    val assessmentSummary = anAssessmentSummary()
    whenever(offenderService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(assessmentSummary)

    val initialChecks = service.getCurrentInitialChecks(PRISON_NUMBER)

    assertThat(initialChecks.assessmentSummary).isEqualTo(assessmentSummary)

    assertThat(initialChecks.eligibility).hasSize(11)
    assertThat(initialChecks.eligibility).allMatch { it.status == EligibilityStatus.NOT_STARTED }

    assertThat(initialChecks.suitability).hasSize(7)
    assertThat(initialChecks.suitability).allMatch { it.status == SuitabilityStatus.NOT_STARTED }
  }

  @Nested
  inner class IsComplete {
    @Test
    fun noProgress() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun someInProgress() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun allInProgress() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun unsuitableButNotIneligible() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun ineligibleWithoutCompleteSuitability() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun ineligibleAndSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun eligibleAndSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun someButNotAllEligibleAndSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE), anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun mixedEligibleAndInProgressSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE), anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isComplete(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }
  }

  @Nested
  inner class IsChecksPassed {
    @Test
    fun noProgress() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.NOT_STARTED))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun someInProgress() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = NOT_STARTED))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun allInProgress() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun unsuitableButNotIneligible() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.UNSUITABLE))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun ineligibleWithoutCompleteSuitability() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.IN_PROGRESS))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun ineligibleAndSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun eligibleAndSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(true)
    }

    @Test
    fun someButNotAllEligibleAndSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE), anEligibilityCheckDetails().copy(status = IN_PROGRESS))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }

    @Test
    fun mixedEligibleAndInProgressSuitable() {
      val eligibilityDetails = listOf(anEligibilityCheckDetails().copy(status = ELIGIBLE), anEligibilityCheckDetails().copy(status = INELIGIBLE))
      val suitabilityStatus = listOf(anSuitabilityCheckDetails().copy(status = SuitabilityStatus.SUITABLE))

      assertThat(service.isChecksPassed(eligibilityDetails, suitabilityStatus)).isEqualTo(false)
    }
  }
}
