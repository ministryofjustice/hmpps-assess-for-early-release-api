package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.CaseloadService.Companion.DAYS_BEFORE_SENTENCE_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anStatusChangedEvent
import java.time.LocalDate
import java.time.Month

class OffenderSummaryResponseMapperTest {

  private val mapper = OffenderSummaryResponseMapper()

  @Test
  fun `should map assessment to offender summary`() {
    val workingDaysToHdced = 12
    val crd = LocalDate.of(2026, Month.MAY, 6)
    val offender = anOffender().copy(crd = crd)
    val anAssessment = anAssessment(offender)
    anAssessment.lastUpdateByUserEvent = anStatusChangedEvent(anAssessment)
    val offenderSummary = mapper.map(anAssessment, workingDaysToHdced)

    assertThat(offenderSummary.prisonNumber).isEqualTo(offender.prisonNumber)
    assertThat(offenderSummary.bookingId).isEqualTo(anAssessment.bookingId)
    assertThat(offenderSummary.forename).isEqualTo(offender.forename)
    assertThat(offenderSummary.surname).isEqualTo(offender.surname)
    assertThat(offenderSummary.crd).isEqualTo(crd)
    assertThat(offenderSummary.hdced).isEqualTo(offender.hdced)
    assertThat(offenderSummary.workingDaysToHdced).isEqualTo(workingDaysToHdced)
    assertThat(offenderSummary.probationPractitioner).isEqualTo(anAssessment.responsibleCom?.fullName)
    assertThat(offenderSummary.isPostponed).isEqualTo(anAssessment.status == AssessmentStatus.POSTPONED)
    assertThat(offenderSummary.postponementReasons).isEqualTo(anAssessment.postponementReasons.map { reason -> reason.reasonType }.toList())
    assertThat(offenderSummary.status).isEqualTo(anAssessment.status)
    assertThat(offenderSummary.addressChecksComplete).isEqualTo(anAssessment.addressChecksComplete)
    assertThat(offenderSummary.currentTask).isEqualTo(anAssessment.currentTask())
    assertThat(offenderSummary.taskOverdueOn).isEqualTo(offender.sentenceStartDate?.plusDays(DAYS_BEFORE_SENTENCE_START))
    assertThat(offenderSummary.crn).isEqualTo(offender.crn)
    assertThat(offenderSummary.lastUpdateBy).isEqualTo("prison user")
  }
}
