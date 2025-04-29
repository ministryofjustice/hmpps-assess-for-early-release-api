package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress

object StatusHelpers {
  fun List<EligibilityCriterionProgress>.toStatus() = when {
    all { it.status == ELIGIBLE } -> ELIGIBLE
    any { it.status == INELIGIBLE } -> INELIGIBLE
    any { it.status == ELIGIBLE } -> IN_PROGRESS
    else -> NOT_STARTED
  }

  fun List<SuitabilityCriterionProgress>.toStatus() = when {
    all { it.status == SUITABLE } -> SUITABLE
    any { it.status == UNSUITABLE } -> UNSUITABLE
    any { it.status == SUITABLE } -> SuitabilityStatus.IN_PROGRESS
    else -> SuitabilityStatus.NOT_STARTED
  }

  fun List<SuitabilityCriterionProgress>.getUnsuitableTaskName() = this.filter { it.status == UNSUITABLE }.map { it.taskName }

  fun List<EligibilityCriterionProgress>.getIneligibleTaskName() = this.filter { it.status == INELIGIBLE }.map { it.taskName }

  fun List<SuitabilityCriterionProgress>.getUnsuitableReasons() = this.filter { it.status == UNSUITABLE }.map { it.questions.map { it.failedQuestionDescription } }.flatten()

  fun List<EligibilityCriterionProgress>.getIneligibleReasons() = this.filter { it.status == INELIGIBLE }.map { it.questions.map { it.failedQuestionDescription } }.flatten()

  fun AssessmentWithEligibilityProgress.calculateAggregateEligibilityStatus(): EligibilityStatus {
    val eligibility = getEligibilityProgress()
    val suitability = getSuitabilityProgress()

    val ineligible = eligibility.any { it.status == INELIGIBLE }
    val unsuitable = suitability.any { it.status == UNSUITABLE }

    val eligible = eligibility.all { it.status == ELIGIBLE }
    val suitable = suitability.all { it.status == SUITABLE }

    val eligibilityHasProgress = eligibility.toStatus() != NOT_STARTED
    val suitabilityHasProgress = suitability.toStatus() != SuitabilityStatus.NOT_STARTED
    assert(!(suitabilityHasProgress && !eligible)) { "Should not be possible to start suitability without being eligible" }

    return when {
      eligible && suitable -> ELIGIBLE
      ineligible || unsuitable -> INELIGIBLE
      eligibilityHasProgress || suitabilityHasProgress -> IN_PROGRESS
      else -> NOT_STARTED
    }
  }

  fun EligibilityCheckResult?.getEligibilityStatus() = this?.let {
    if (it.criterionMet) ELIGIBLE else INELIGIBLE
  } ?: NOT_STARTED

  fun EligibilityCheckResult?.getSuitabilityStatus() = this?.let {
    if (it.criterionMet) SUITABLE else UNSUITABLE
  } ?: SuitabilityStatus.NOT_STARTED

  fun EligibilityCheckResult?.getAnswer(questionName: String) = this?.let { questionAnswers[questionName] }
}
