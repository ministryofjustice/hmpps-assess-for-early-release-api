package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
  private val offenderSummaryResponseMapper: OffenderSummaryResponseMapper,
) {

  companion object {
    const val DAYS_BEFORE_SENTENCE_START = 10L
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getCaseAdminCaseload(prisonCode: String): List<OffenderSummaryResponse> {
    log.info("Fetching caseload for staffCode: $prisonCode")
    val assessments = assessmentRepository.findByOffenderPrisonIdAndDeletedTimestampIsNull(prisonCode)
    log.info("assessments: $assessments")
    return assessments.map { createOffenderSummary(it) }
  }

  @Transactional
  fun getComCaseload(staffCode: String): List<OffenderSummaryResponse> {
    log.info("Fetching caseload for staffCode: $staffCode")
    val assessments = assessmentRepository.findByResponsibleComStaffCodeAndDeletedTimestampIsNull(staffCode)
    log.info("assessments: $assessments")
    return assessments.map { createOffenderSummary(it) }
  }

  @Transactional
  fun getDecisionMakerCaseload(prisonCode: String): List<OffenderSummaryResponse> {
    val assessments = assessmentRepository.findByOffenderPrisonIdAndDeletedTimestampIsNull(prisonCode)
    return assessments.map { createOffenderSummary(it) }
  }

  fun createOffenderSummary(assessments: Assessment): OffenderSummaryResponse = offenderSummaryResponseMapper.map(assessments, workingDaysService.workingDaysUntil(assessments.offender.hdced))
}
