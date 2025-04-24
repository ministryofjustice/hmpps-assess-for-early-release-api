package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.CaseloadService.Companion.DAYS_BEFORE_SENTENCE_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.STAFF_CODE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderSummaryResponseMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays.BankHolidayService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays.WorkingDaysService
import java.time.Clock
import java.time.LocalDate

class CaseloadServiceTest {
  private val assessmentRepository = mock<AssessmentRepository>()
  private val bankHolidayService = mock<BankHolidayService>()
  private val workingDaysService = WorkingDaysService(bankHolidayService, Clock.systemDefaultZone())
  private val offenderSummaryResponseMapper = OffenderSummaryResponseMapper()
  private val probationService = mock<ProbationService>()

  private val service: CaseloadService =
    CaseloadService(
      assessmentRepository,
      workingDaysService,
      offenderSummaryResponseMapper,
      probationService,
    )

  @Test
  fun `should get the case admin case load`() {
    val offender1 = anOffender(sentenceStartDate = LocalDate.now().minusDays(5))
    val offender2 =
      offender1.copy(
        id = offender1.id + 1,
        prisonNumber = "ZX2318KD",
        sentenceStartDate = LocalDate.now().minusDays(11),
      )
    val offender3 = offender1.copy(
      id = offender1.id + 2,
      prisonNumber = "ZX2318KJ",
      sentenceStartDate = null,
    )
    whenever(assessmentRepository.findByOffenderPrisonIdAndDeletedTimestampIsNull(PRISON_ID)).thenReturn(
      listOf(
        offender1.assessments.first(),
        offender2.assessments.first().copy(offender = offender2),
        offender3.assessments.first().copy(offender = offender3),
      ),
    )

    val caseload = service.getCaseAdminCaseload(PRISON_ID)
    assertThat(caseload.size).isEqualTo(3)
    assertThat(caseload).containsExactlyInAnyOrder(
      OffenderSummaryResponse(
        prisonNumber = offender1.prisonNumber,
        bookingId = offender1.assessments.first().bookingId,
        forename = offender1.forename!!,
        surname = offender1.surname!!,
        hdced = offender1.assessments.first().hdced,
        workingDaysToHdced = 5,
        probationPractitioner = offender1.assessments.first().responsibleCom?.fullName,
        status = AssessmentStatus.NOT_STARTED,
        addressChecksComplete = false,
        currentTask = Task.ASSESS_ELIGIBILITY,
        taskOverdueOn = offender1.sentenceStartDate?.plusDays(DAYS_BEFORE_SENTENCE_START),
      ),
      OffenderSummaryResponse(
        prisonNumber = offender2.prisonNumber,
        bookingId = offender1.assessments.first().bookingId,
        forename = offender2.forename!!,
        surname = offender2.surname!!,
        hdced = offender2.assessments.first().hdced,
        workingDaysToHdced = 5,
        probationPractitioner = offender2.assessments.first().responsibleCom?.fullName,
        status = AssessmentStatus.NOT_STARTED,
        addressChecksComplete = false,
        currentTask = Task.ASSESS_ELIGIBILITY,
        taskOverdueOn = offender2.sentenceStartDate?.plusDays(DAYS_BEFORE_SENTENCE_START),
      ),
      OffenderSummaryResponse(
        prisonNumber = offender3.prisonNumber,
        bookingId = offender1.assessments.first().bookingId,
        forename = offender3.forename!!,
        surname = offender3.surname!!,
        hdced = offender3.assessments.first().hdced,
        workingDaysToHdced = 5,
        probationPractitioner = offender3.assessments.first().responsibleCom?.fullName,
        status = AssessmentStatus.NOT_STARTED,
        addressChecksComplete = false,
        currentTask = Task.ASSESS_ELIGIBILITY,
        taskOverdueOn = null,
      ),
    )
  }

  @Test
  fun `should get the com case load`() {
    // Given
    val assessment1 =
      anOffender().assessments.first().copy(status = AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    val assessment2 = anOffender().assessments.first().copy(status = AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS)
    whenever(
      assessmentRepository.findByResponsibleComStaffCodeAndDeletedTimestampIsNull(STAFF_CODE),
    ).thenReturn(listOf(assessment1, assessment2))

    // When
    val caseload = service.getComCaseload(STAFF_CODE)

    // Then
    assertThat(caseload.size).isEqualTo(2)

    assertThat(caseload.map { it.bookingId }).containsExactlyInAnyOrder(
      assessment1.bookingId,
      assessment2.bookingId,
    )
  }

  @Test
  fun `should get the decision maker case load`() {
    // Given
    val assessment1 =
      anOffender().assessments.first().copy(status = AssessmentStatus.APPROVED)
    val assessment2 = anOffender().assessments.first().copy(status = AssessmentStatus.AWAITING_DECISION)
    whenever(
      assessmentRepository.findByOffenderPrisonIdAndDeletedTimestampIsNull(PRISON_ID),
    ).thenReturn(
      listOf(
        assessment1,
        assessment2,
      ),
    )

    // When
    val caseload = service.getDecisionMakerCaseload(PRISON_ID)

    // Then
    assertThat(caseload.size).isEqualTo(2)
    assertThat(caseload.map { it.bookingId }).containsExactlyInAnyOrder(
      assessment1.bookingId,
      assessment2.bookingId,
    )
  }
}
