package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ADDRESS_REQUEST_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksTaskStatus

class ResidentialChecksServiceTest {
  private val assessmentService = mock<AssessmentService>()
  private val residentialChecksService: ResidentialChecksService = ResidentialChecksService(assessmentService)

  @Test
  fun `should get the status of the residential checks for an assessment`() {
    whenever(assessmentService.getCurrentAssessmentSummary(PRISON_NUMBER)).thenReturn(anAssessmentSummary())

    val residentialChecksView = residentialChecksService.getResidentialChecksView(PRISON_NUMBER, ADDRESS_REQUEST_ID)

    assertThat(residentialChecksView.assessmentSummary).isEqualTo(anAssessmentSummary())
    assertThat(residentialChecksView.overallStatus).isEqualTo(ResidentialChecksStatus.NOT_STARTED)
    assertThat(residentialChecksView.tasks).hasSize(6)
    assertThat(residentialChecksView.tasks).allMatch({ it.status == ResidentialChecksTaskStatus.NOT_STARTED })
  }
}
