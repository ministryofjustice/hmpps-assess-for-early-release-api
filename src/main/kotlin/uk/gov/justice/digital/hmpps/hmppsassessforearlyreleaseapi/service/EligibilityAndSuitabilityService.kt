package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityAndSuitabilityCaseView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.toStatus

@Service
class EligibilityAndSuitabilityService(
  private val policyService: PolicyService,
  private val assessmentService: AssessmentService,
  private val assessmentRepository: AssessmentRepository,
  private val assessmentLifecycleService: AssessmentLifecycleService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getCaseView(prisonNumber: String): EligibilityAndSuitabilityCaseView {
    val assessment = assessmentService.getCurrentAssessment(prisonNumber)

    return EligibilityAndSuitabilityCaseView(
      assessmentSummary = createAssessmentSummary(assessment),
      complete = StatusHelpers.isComplete(assessment.eligibilityProgress, assessment.suitabilityProgress),
      checksPassed = StatusHelpers.isChecksPassed(assessment.eligibilityProgress, assessment.suitabilityProgress),
      eligibility = assessment.eligibilityProgress,
      eligibilityStatus = assessment.eligibilityProgress.toStatus(),
      suitability = assessment.suitabilityProgress,
      suitabilityStatus = assessment.suitabilityProgress.toStatus(),
    )
  }

  @Transactional
  fun getEligibilityCriterionView(prisonNumber: String, code: String): EligibilityCriterionView {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    val eligibilityProgress = currentAssessment.eligibilityProgress.dropWhile { it.code != code }.take(2)
    if (eligibilityProgress.isEmpty()) throw EntityNotFoundException("Cannot find criterion with code $code")

    return EligibilityCriterionView(
      assessmentSummary = createAssessmentSummary(currentAssessment),
      criterion = eligibilityProgress[0],
      nextCriterion = eligibilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun getSuitabilityCriterionView(prisonNumber: String, code: String): SuitabilityCriterionView {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    val suitabilityProgress = currentAssessment.suitabilityProgress.dropWhile { it.code != code }.take(2)
    if (suitabilityProgress.isEmpty()) throw EntityNotFoundException("Cannot find criterion with code $code")

    return SuitabilityCriterionView(
      assessmentSummary = createAssessmentSummary(currentAssessment),
      criterion = suitabilityProgress[0],
      nextCriterion = suitabilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun saveAnswer(prisonNumber: String, answer: CriterionCheck) {
    log.info("Saving answer: $prisonNumber, $answer")

    val criterionType = CriterionType.valueOf(answer.type.name)
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)

    with(currentAssessment) {
      val criterion = policyService.getCriterion(assessmentEntity.policyVersion, criterionType, answer.code)
      val criterionMet = criterion.isMet(answer.answers)

      assessmentEntity.addOrReplaceEligibilityCriterionResult(criterionType, criterion.code, criterionMet, answer.answers)
      assessmentEntity.changeStatus(assessmentLifecycleService.eligibilityAnswerSubmitted(currentAssessment))
      assessmentRepository.save(assessmentEntity)
    }
  }

  private fun createAssessmentSummary(
    assessmentWithEligibilityProgress: AssessmentWithEligibilityProgress,
  ) = with(assessmentWithEligibilityProgress) {
    AssessmentSummary(
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = prison,
      status = assessmentEntity.status,
      policyVersion = assessmentEntity.policyVersion,
    )
  }
}
