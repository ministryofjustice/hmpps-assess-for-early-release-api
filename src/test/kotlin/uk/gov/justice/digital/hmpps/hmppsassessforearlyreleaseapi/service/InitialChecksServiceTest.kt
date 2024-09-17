package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary

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
    assertThat(initialChecks.eligibility).allMatch { it.state == EligibilityState.NOT_STARTED }

    assertThat(initialChecks.suitability).hasSize(7)
    assertThat(initialChecks.suitability).allMatch { it.state == SuitabilityState.NOT_STARTED }
  }
}
