package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays.WorkingDaysService

@Service
class CaseloadService(
  private val assessmentRepository: AssessmentRepository,
  private val workingDaysService: WorkingDaysService,
) {
  @Transactional
  fun getCaseAdminCaseload(prisonCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByOffenderPrisonId(prisonCode)
    return assessments.map { it.toOffenderSummary() }
  }

  @Transactional
  fun getComCaseload(staffCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByResponsibleComStaffCode(staffCode)
    return assessments.map { it.toOffenderSummary() }
  }

  @Transactional
  fun getDecisionMakerCaseload(prisonCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByOffenderPrisonId(prisonCode)
    return assessments.map { it.toOffenderSummary() }
  }

  fun Assessment.toOffenderSummary() = OffenderSummaryResponse(
    prisonNumber = offender.prisonNumber,
    bookingId = offender.bookingId,
    forename = offender.forename!!,
    surname = offender.surname!!,
    hdced = offender.hdced,
    workingDaysToHdced = workingDaysService.workingDaysBefore(offender.hdced),
    probationPractitioner = this.responsibleCom?.fullName,
    isPostponed = this.status == AssessmentStatus.POSTPONED,
    postponementDate = this.postponementDate,
    postponementReasons = this.postponementReasons.map { reason -> reason.reasonType }.toList(),
    status = this.status,
    addressChecksComplete = this.addressChecksComplete,
    currentTask = this.currentTask(),
    taskOverdueOn = offender.sentenceStartDate?.plusDays(DAYS_BEFORE_SENTENCE_START),
    crn = offender.crn,
  )

  companion object {
    const val DAYS_BEFORE_SENTENCE_START = 10L
  }
}
