package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityChecksFailed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityChecksPassed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityAndSuitabilityCaseView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.FailureType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getIneligibleTaskName
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getUnsuitableTaskName
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.toStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper

@Service
class EligibilityAndSuitabilityService(
  private val policyService: PolicyService,
  private val assessmentService: AssessmentService,
  private val offenderToAssessmentSummaryMapper: OffenderToAssessmentSummaryMapper,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getCaseView(prisonNumber: String): EligibilityAndSuitabilityCaseView = eligibilityAndSuitabilityCaseView(getCurrentAssessmentWithEligibilityProgress(prisonNumber))

  @Transactional
  fun getEligibilityCriterionView(prisonNumber: String, code: String): EligibilityCriterionView {
    val currentAssessment = getCurrentAssessmentWithEligibilityProgress(prisonNumber)
    val eligibilityProgress = currentAssessment.getEligibilityProgress().dropWhile { it.code != code }.take(2)
    if (eligibilityProgress.isEmpty()) throw ItemNotFoundException("Cannot find criterion with code $code")

    return EligibilityCriterionView(
      assessmentSummary = offenderToAssessmentSummaryMapper.map(currentAssessment.assessmentEntity),
      criterion = eligibilityProgress[0],
      nextCriterion = eligibilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun getSuitabilityCriterionView(prisonNumber: String, code: String): SuitabilityCriterionView {
    val currentAssessment = getCurrentAssessmentWithEligibilityProgress(prisonNumber)
    val suitabilityProgress = currentAssessment.getSuitabilityProgress().dropWhile { it.code != code }.take(2)
    if (suitabilityProgress.isEmpty()) throw ItemNotFoundException("Cannot find criterion with code $code")

    return SuitabilityCriterionView(
      assessmentSummary = offenderToAssessmentSummaryMapper.map(currentAssessment.assessmentEntity),
      criterion = suitabilityProgress[0],
      nextCriterion = suitabilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun saveAnswer(prisonNumber: String, answer: CriterionCheck): EligibilityAndSuitabilityCaseView {
    log.info("Saving answer: $prisonNumber, $answer")

    val criterionType = CriterionType.valueOf(answer.type.name)
    val currentAssessment = getCurrentAssessmentWithEligibilityProgress(prisonNumber)

    with(currentAssessment) {
      val criterion = policyService.getCriterion(assessmentEntity.policyVersion, criterionType, answer.code)
      val criterionMet = criterion.isMet(answer.answers)

      assessmentEntity.addOrReplaceEligibilityCriterionResult(
        criterionType,
        criterion.code,
        criterionMet,
        answer.answers,
        answer.agent,
      )

      val eligibilityStatus = currentAssessment.calculateAggregateEligibilityStatus()

      val event = when (eligibilityStatus) {
        INELIGIBLE -> EligibilityChecksFailed(answer.type, answer.code, answer.answers)
        ELIGIBLE -> EligibilityChecksPassed(answer.type, answer.code, answer.answers)
        IN_PROGRESS -> EligibilityAnswerProvided(answer.type, answer.code, answer.answers)
        else -> error("Should not be possible to have a status of $eligibilityStatus")
      }

      assessmentService.transitionAssessment(assessmentEntity, event, answer.agent)
      return eligibilityAndSuitabilityCaseView(currentAssessment)
    }
  }

  private fun getCurrentAssessmentWithEligibilityProgress(prisonNumber: String): AssessmentWithEligibilityProgress {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    val policy = policyService.getVersionFromPolicy(currentAssessment.policyVersion)
    return AssessmentWithEligibilityProgress(
      assessmentEntity = currentAssessment,
      policy = policy,
    )
  }

  private fun eligibilityAndSuitabilityCaseView(currentAssessment: AssessmentWithEligibilityProgress): EligibilityAndSuitabilityCaseView {
    val eligibility = currentAssessment.getEligibilityProgress()
    val eligibilityStatus = eligibility.toStatus()
    val suitability = currentAssessment.getSuitabilityProgress()
    val suitabilityStatus = suitability.toStatus()

    return EligibilityAndSuitabilityCaseView(
      assessmentSummary = offenderToAssessmentSummaryMapper.map(currentAssessment.assessmentEntity),
      overallStatus = currentAssessment.calculateAggregateEligibilityStatus(),
      eligibility = eligibility,
      eligibilityStatus = eligibilityStatus,
      suitability = suitability,
      suitabilityStatus = suitabilityStatus,
      failureType = when {
        eligibilityStatus == INELIGIBLE -> FailureType.INELIGIBLE
        suitabilityStatus == UNSUITABLE -> FailureType.UNSUITABLE
        else -> null
      },
      failedCheckReasons = eligibility.getIneligibleTaskName() + suitability.getUnsuitableTaskName(),
    )
  }
}
