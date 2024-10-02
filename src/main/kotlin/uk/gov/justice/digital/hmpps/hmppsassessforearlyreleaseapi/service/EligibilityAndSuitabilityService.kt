package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaType.SUITABILITY
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

    val criteriaChecks = assessment.criteriaCheck

    val existingCriteria =
      criteriaChecks.find { it.criteriaType == CriteriaType.valueOf(answer.type.name) && it.criteriaCode == answer.code }

    val criteria = when {
      existingCriteria != null -> {
        criteriaChecks.remove(existingCriteria)
        existingCriteria.copy(
          criteriaMet = answer.answers.all { it.value },
          questionAnswers = answer.answers.toMap(HashMap()),
          lastUpdatedTimestamp = LocalDateTime.now(),
        )
      }

      else -> CriteriaCheck(
        assessment = assessment,
        criteriaMet = answer.answers.all { it.value },
        questionAnswers = answer.answers.toMap(HashMap()),
        criteriaCode = answer.code,
        criteriaVersion = assessment.policyVersion,
        criteriaType = CriteriaType.valueOf(answer.type.name),
      )
    }
    criteriaChecks.add(criteria)
    assessmentRepository.save(assessment)
  }

  /**
   * Combines eligibility criteria from the policy with the checks on that criteria for a given case
   */
  private fun Assessment.getEligibilityProgress(): List<EligibilityCriterionProgress> {
    val policy = policyService.getVersionFromPolicy(this.policyVersion)
    val codeToChecks = this.criteriaCheck
      .filter { it.criteriaType == ELIGIBILITY }
      .associateBy { it.criteriaCode }

    return policy.eligibilityCriteria.map { it.toEligibilityCriterionProgress(codeToChecks[it.code]) }
  }

  private fun Criterion.toEligibilityCriterionProgress(criteriaCheck: CriteriaCheck?) = EligibilityCriterionProgress(
    code = code,
    taskName = name,
    status = criteriaCheck.getEligibilityStatus(),
    questions = questions.map {
      Question(
        text = it.text,
        hint = it.hint,
        name = it.name,
        answer = criteriaCheck.getAnswer(it.name),
      )
    },
  )

  /**
   * Combines suitability criteria from the policy with the checks on that criteria for a given case
   */
  private fun Assessment.getSuitabilityProgress(): List<SuitabilityCriterionProgress> {
    val policy = policyService.getVersionFromPolicy(this.policyVersion)
    val codeToChecks = this.criteriaCheck
      .filter { it.criteriaType == SUITABILITY }
      .associateBy { it.criteriaCode }

    return policy.suitabilityCriteria.map { it.toSuitabilityCriterionProgress(codeToChecks[it.code]) }
  }

  private fun Criterion.toSuitabilityCriterionProgress(criteriaCheck: CriteriaCheck?) =
    SuitabilityCriterionProgress(
      code = code,
      taskName = name,
      status = criteriaCheck.getSuitabilityStatus(),
      questions = questions.map {
        Question(
          text = it.text,
          hint = it.hint,
          name = it.name,
          answer = criteriaCheck.getAnswer(it.name),
        )
      },
    )

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
