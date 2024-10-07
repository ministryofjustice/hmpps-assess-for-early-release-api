package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityAndSuitabilityCaseView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getSuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.toStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService
import java.time.LocalDateTime

@Service
class EligibilityAndSuitabilityService(
  private val policyService: PolicyService,
  private val prisonRegisterService: PrisonRegisterService,
  private val assessmentRepository: AssessmentRepository,
  private val offenderRepository: OffenderRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getCaseView(prisonNumber: String): EligibilityAndSuitabilityCaseView {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

    val currentAssessment = offender.currentAssessment()
    val eligibilityProgress = currentAssessment.getEligibilityProgress()
    val suitabilityProgress = currentAssessment.getSuitabilityProgress()

    return EligibilityAndSuitabilityCaseView(
      assessmentSummary = createAssessmentSummary(offender, currentAssessment),
      complete = StatusHelpers.isComplete(eligibilityProgress, suitabilityProgress),
      checksPassed = StatusHelpers.isChecksPassed(eligibilityProgress, suitabilityProgress),
      eligibility = eligibilityProgress,
      eligibilityStatus = eligibilityProgress.toStatus(),
      suitability = suitabilityProgress,
      suitabilityStatus = suitabilityProgress.toStatus(),
    )
  }

  @Transactional
  fun getEligibilityCriterionView(prisonNumber: String, code: String): EligibilityCriterionView {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

    val currentAssessment = offender.currentAssessment()
    val eligibilityProgress = currentAssessment.getEligibilityProgress().dropWhile { it.code != code }.take(2)
    if (eligibilityProgress.isEmpty()) throw EntityNotFoundException("Cannot find criterion with code $code")

    return EligibilityCriterionView(
      assessmentSummary = createAssessmentSummary(offender, currentAssessment),
      criterion = eligibilityProgress[0],
      nextCriterion = eligibilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun getSuitabilityCriterionView(prisonNumber: String, code: String): SuitabilityCriterionView {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

    val currentAssessment = offender.currentAssessment()
    val suitabilityProgress = currentAssessment.getSuitabilityProgress().dropWhile { it.code != code }.take(2)
    if (suitabilityProgress.isEmpty()) throw EntityNotFoundException("Cannot find criterion with code $code")

    return SuitabilityCriterionView(
      assessmentSummary = createAssessmentSummary(offender, currentAssessment),
      criterion = suitabilityProgress[0],
      nextCriterion = suitabilityProgress.getOrNull(1),
    )
  }

  @Transactional
  fun saveAnswer(prisonNumber: String, answer: CriterionCheck) {
    log.info("Saving answer: $prisonNumber, $answer")
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")
    val assessment = offender.currentAssessment()
    val criterionType = CriterionType.valueOf(answer.type.name)

    val currentResults = assessment.eligibilityCheckResults
    val criterion = policyService.getCriterion(assessment.policyVersion, criterionType, answer.code)
    val existingCriterionResult = currentResults.find(criterionType, answer)

    val criteria = when {
      existingCriterionResult != null -> {
        currentResults.remove(existingCriterionResult)
        existingCriterionResult.copy(
          criterionMet = criterion.isMet(answer.answers),
          questionAnswers = answer.answers,
          lastUpdatedTimestamp = LocalDateTime.now(),
        )
      }

      else -> EligibilityCheckResult(
        assessment = assessment,
        criterionMet = criterion.isMet(answer.answers),
        questionAnswers = answer.answers,
        criterionCode = answer.code,
        criterionVersion = assessment.policyVersion,
        criterionType = criterionType,
      )
    }
    currentResults.add(criteria)
    assessmentRepository.save(assessment)
  }

  /**
   * Combines eligibility criteria from the policy with the checks on that criteria for a given case
   */
  private fun Assessment.getEligibilityProgress(): List<EligibilityCriterionProgress> {
    val policy = policyService.getVersionFromPolicy(this.policyVersion)
    val codeToChecks = this.eligibilityCheckResults
      .filter { it.criterionType == ELIGIBILITY }
      .associateBy { it.criterionCode }

    return policy.eligibilityCriteria.map { it.toEligibilityCriterionProgress(codeToChecks[it.code]) }
  }

  private fun Criterion.toEligibilityCriterionProgress(eligibilityCheckResult: EligibilityCheckResult?) =
    EligibilityCriterionProgress(
      code = code,
      taskName = name,
      status = eligibilityCheckResult.getEligibilityStatus(),
      questions = questions.map {
        Question(
          text = it.text,
          hint = it.hint,
          name = it.name,
          answer = eligibilityCheckResult.getAnswer(it.name),
        )
      },
    )

  /**
   * Combines suitability criteria from the policy with the checks on that criteria for a given case
   */
  private fun Assessment.getSuitabilityProgress(): List<SuitabilityCriterionProgress> {
    val policy = policyService.getVersionFromPolicy(this.policyVersion)
    val codeToChecks = this.eligibilityCheckResults
      .filter { it.criterionType == SUITABILITY }
      .associateBy { it.criterionCode }

    return policy.suitabilityCriteria.map { it.toSuitabilityCriterionProgress(codeToChecks[it.code]) }
  }

  private fun Criterion.toSuitabilityCriterionProgress(eligibilityCheckResult: EligibilityCheckResult?) =
    SuitabilityCriterionProgress(
      code = code,
      taskName = name,
      status = eligibilityCheckResult.getSuitabilityStatus(),
      questions = questions.map {
        Question(
          text = it.text,
          hint = it.hint,
          name = it.name,
          answer = eligibilityCheckResult.getAnswer(it.name),
        )
      },
    )

  private fun Set<EligibilityCheckResult>.find(
    criterionType: CriterionType,
    answer: CriterionCheck,
  ) = this.find { it.criterionType == criterionType && it.criterionCode == answer.code }

  private fun createAssessmentSummary(
    offender: Offender,
    currentAssessment: Assessment,
  ) = AssessmentSummary(
    forename = offender.forename,
    surname = offender.surname,
    dateOfBirth = offender.dateOfBirth,
    prisonNumber = offender.prisonNumber,
    hdced = offender.hdced,
    crd = offender.crd,
    location = prisonRegisterService.getNameForId(offender.prisonId),
    status = currentAssessment.status,
    policyVersion = currentAssessment.policyVersion,
  )
}
