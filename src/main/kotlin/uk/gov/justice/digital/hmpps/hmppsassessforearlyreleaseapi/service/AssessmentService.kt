package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getSuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService

@Service
class AssessmentService(
  private val policyService: PolicyService,
  private val prisonRegisterService: PrisonRegisterService,
  private val offenderRepository: OffenderRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getCurrentAssessment(prisonNumber: String): AssessmentWithEligibilityProgress {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

    val currentAssessment = offender.currentAssessment()
    val eligibilityProgress = currentAssessment.getEligibilityProgress()
    val suitabilityProgress = currentAssessment.getSuitabilityProgress()

    return AssessmentWithEligibilityProgress(
      offender = offender,
      assessmentEntity = currentAssessment,
      prison = prisonRegisterService.getNameForId(offender.prisonId),
      eligibilityProgress = eligibilityProgress,
      suitabilityProgress = suitabilityProgress,
    )
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

  data class AssessmentWithEligibilityProgress(
    val offender: Offender,
    val assessmentEntity: Assessment,
    val prison: String,
    val eligibilityProgress: List<EligibilityCriterionProgress>,
    val suitabilityProgress: List<SuitabilityCriterionProgress>,
  )
}
