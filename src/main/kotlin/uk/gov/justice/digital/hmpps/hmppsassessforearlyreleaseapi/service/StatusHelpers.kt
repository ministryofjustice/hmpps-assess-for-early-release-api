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
  private val IN_PROGRESS_SUITABILITY = setOf(SuitabilityStatus.NOT_STARTED)

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

  fun AssessmentWithEligibilityProgress.calculateAggregateStatus(): EligibilityStatus {
    val eligibility = eligibilityProgress()
    val suitability = suitabilityProgress()

    val ineligible = eligibility.any { it.status == INELIGIBLE }
    val eligible = eligibility.all { it.status == ELIGIBLE }
    val suitable = suitability.all { it.status == SUITABLE }

    val suitabilityComplete = suitability.none { IN_PROGRESS_SUITABILITY.contains(it.status) }
    val eligibilityHasProgress = eligibility.toStatus() != NOT_STARTED
    val suitabilityHasProgress = suitability.toStatus() != SuitabilityStatus.NOT_STARTED

    return when {
      eligible && suitable -> ELIGIBLE
      (eligible && suitabilityComplete) || ineligible -> INELIGIBLE
      eligibilityHasProgress || suitabilityHasProgress -> IN_PROGRESS
      else -> NOT_STARTED
    }
  }

  fun EligibilityCheckResult?.getEligibilityStatus() =
    this?.let {
      if (it.criterionMet) ELIGIBLE else INELIGIBLE
    } ?: NOT_STARTED

  fun EligibilityCheckResult?.getSuitabilityStatus() =
    this?.let {
      if (it.criterionMet) SUITABLE else UNSUITABLE
    } ?: SuitabilityStatus.NOT_STARTED

  fun EligibilityCheckResult?.getAnswer(questionName: String) =
    this?.let { questionAnswers[questionName] }
}
