package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.EligibilityAndSuitabilityAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityAndSuitabilityCaseView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.FailureType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getIneligibleReasons
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getUnsuitableReasons
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.toStatus

@Service
class EligibilityAndSuitabilityService(
  private val policyService: PolicyService,
  private val assessmentService: AssessmentService,
  private val assessmentRepository: AssessmentRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun AssessmentWithEligibilityProgress.toSummary() = with(this) {
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
        tasks = assessmentEntity.status.tasks().mapValues { (_, tasks) ->
          tasks.map { TaskProgress(it.task, it.status(assessmentEntity)) }
        },
      )
    }
  }

  @Transactional
  fun getCaseView(prisonNumber: String): EligibilityAndSuitabilityCaseView {
    val assessment = assessmentService.getCurrentAssessment(prisonNumber)

    val eligibility = assessment.getEligibilityProgress()
    val eligibilityStatus = eligibility.toStatus()
    val suitability = assessment.getSuitabilityProgress()
    val suitabilityStatus = suitability.toStatus()

    return EligibilityAndSuitabilityCaseView(
      assessmentSummary = assessment.toSummary(),
      overallStatus = assessment.calculateAggregateEligibilityStatus(),
      eligibility = eligibility,
      eligibilityStatus = eligibilityStatus,
      suitability = suitability,
      suitabilityStatus = suitabilityStatus,
      failureType = when {
        eligibilityStatus == INELIGIBLE -> FailureType.INELIGIBLE
        suitabilityStatus == UNSUITABLE -> FailureType.UNSUITABLE
        else -> null
      },
      failedCheckReasons = eligibility.getIneligibleReasons() + suitability.getUnsuitableReasons(),
    )
  }

  @Transactional
  fun getEligibilityCriterionView(prisonNumber: String, code: String): EligibilityCriterionView {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    val eligibilityProgress = currentAssessment.getEligibilityProgress().dropWhile { it.code != code }.take(2)
    if (eligibilityProgress.isEmpty()) throw EntityNotFoundException("Cannot find criterion with code $code")

    return EligibilityCriterionView(
      assessmentSummary = currentAssessment.toSummary(),
      criterion = eligibilityProgress[0],
      nextCriterion = eligibilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun getSuitabilityCriterionView(prisonNumber: String, code: String): SuitabilityCriterionView {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    val suitabilityProgress = currentAssessment.getSuitabilityProgress().dropWhile { it.code != code }.take(2)
    if (suitabilityProgress.isEmpty()) throw EntityNotFoundException("Cannot find criterion with code $code")

    return SuitabilityCriterionView(
      assessmentSummary = currentAssessment.toSummary(),
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

      assessmentEntity.addOrReplaceEligibilityCriterionResult(
        criterionType,
        criterion.code,
        criterionMet,
        answer.answers,
      )

      val eligibilityStatus = currentAssessment.calculateAggregateEligibilityStatus()
      assessmentEntity.performTransition(EligibilityAndSuitabilityAnswerProvided(eligibilityStatus))
      assessmentRepository.save(assessmentEntity)
    }
  }
}
