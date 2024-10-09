package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress

object StatusHelpers {

  fun List<EligibilityCriterionProgress>.toStatus() = when {
    all { it.status == ELIGIBLE } -> ELIGIBLE
    any { it.status == INELIGIBLE } -> INELIGIBLE
    any { it.status == ELIGIBLE } -> IN_PROGRESS
    else -> EligibilityStatus.NOT_STARTED
  }

  fun List<SuitabilityCriterionProgress>.toStatus() = when {
    all { it.status == SUITABLE } -> SUITABLE
    any { it.status == UNSUITABLE } -> UNSUITABLE
    any { it.status == SUITABLE } -> SuitabilityStatus.IN_PROGRESS
    else -> SuitabilityStatus.NOT_STARTED
  }

  fun AssessmentWithEligibilityProgress.isComplete() = isComplete(eligibilityProgress, suitabilityProgress)
  fun AssessmentWithEligibilityProgress.isChecksPassed() = isChecksPassed(eligibilityProgress, suitabilityProgress)
  fun AssessmentWithEligibilityProgress.inProgress() = inProgress(eligibilityProgress, suitabilityProgress)

  fun isComplete(
    eligibilityProgress: List<EligibilityCriterionProgress>,
    suitabilityProgress: List<SuitabilityCriterionProgress>,
  ): Boolean {
    val eligibilityStatus = eligibilityProgress.toStatus()
    val suitabilityStatus = suitabilityProgress.toStatus()

    val ineligible = eligibilityStatus == INELIGIBLE
    val eligible = eligibilityStatus == ELIGIBLE
    val suitabilityComplete = suitabilityStatus == SUITABLE || suitabilityStatus == UNSUITABLE

    return (eligible && suitabilityComplete) || ineligible
  }

  fun isChecksPassed(
    eligibilityProgress: List<EligibilityCriterionProgress>,
    suitabilityProgress: List<SuitabilityCriterionProgress>,
  ): Boolean {
    val eligible = eligibilityProgress.toStatus() == ELIGIBLE
    val suitabilityPassed = suitabilityProgress.toStatus() == SUITABLE
    return (eligible && suitabilityPassed)
  }

  fun inProgress(
    eligibilityProgress: List<EligibilityCriterionProgress>,
    suitabilityProgress: List<SuitabilityCriterionProgress>,
  ): Boolean {
    if (eligibilityProgress.toStatus() == IN_PROGRESS) return true
    return suitabilityProgress.toStatus() == SuitabilityStatus.IN_PROGRESS
  }

  fun EligibilityCheckResult?.getEligibilityStatus() =
    this?.let {
      if (it.criterionMet) ELIGIBLE else INELIGIBLE
    } ?: EligibilityStatus.NOT_STARTED

  fun EligibilityCheckResult?.getSuitabilityStatus() =
    this?.let {
      if (it.criterionMet) SUITABLE else UNSUITABLE
    } ?: NOT_STARTED

  fun EligibilityCheckResult?.getAnswer(questionName: String) =
    this?.let { questionAnswers[questionName] }
}
