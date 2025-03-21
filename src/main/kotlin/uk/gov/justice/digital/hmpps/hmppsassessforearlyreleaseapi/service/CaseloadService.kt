package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderSummaryResponseMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays.WorkingDaysService

@Service
class CaseloadService(
  private val assessmentRepository: AssessmentRepository,
  private val workingDaysService: WorkingDaysService,
  private val offenderSummaryResponseMapper : OffenderSummaryResponseMapper
) {

  companion object {
    const val DAYS_BEFORE_SENTENCE_START = 10L
  }

  @Transactional
  fun getCaseAdminCaseload(prisonCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByOffenderPrisonIdAndDeletedTimestampIsNull(prisonCode)
    return assessments.map { createOffenderSummary(it) }
  }

  @Transactional
  fun getComCaseload(staffCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByResponsibleComStaffCodeAndDeletedTimestampIsNull(staffCode)
    return assessments.map { createOffenderSummary(it)  }
  }

  @Transactional
  fun getDecisionMakerCaseload(prisonCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByOffenderPrisonIdAndDeletedTimestampIsNull(prisonCode)
    return assessments.map { createOffenderSummary(it) }
  }

  fun createOffenderSummary(assessments : Assessment): OffenderSummaryResponse {
    return offenderSummaryResponseMapper.map(assessments, workingDaysService.workingDaysUntil(assessments.offender.hdced))
  }
}
