package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaCheck
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

  fun isComplete(
    eligibilityDetails: List<EligibilityCriterionProgress>,
    suitabilityDetails: List<SuitabilityCriterionProgress>,
  ): Boolean {
    val ineligible = eligibilityDetails.any { it.status == INELIGIBLE }
    val eligible = eligibilityDetails.all { it.status == ELIGIBLE }
    val suitabilityComplete =
      suitabilityDetails.all { it.status == SUITABLE } || suitabilityDetails.any { it.status == UNSUITABLE }

    val complete = (eligible && suitabilityComplete) || ineligible
    return complete
  }

  fun isChecksPassed(
    eligibilityDetails: List<EligibilityCriterionProgress>,
    suitabilityDetails: List<SuitabilityCriterionProgress>,
  ): Boolean {
    val eligible = eligibilityDetails.all { it.status == ELIGIBLE }
    val suitabilityPassed = suitabilityDetails.all { it.status == SUITABLE }
    val checksPassed = (eligible && suitabilityPassed)
    return checksPassed
  }

  fun CriteriaCheck?.getEligibilityStatus() =
    this?.let {
      if (it.criteriaMet) ELIGIBLE else INELIGIBLE
    } ?: EligibilityStatus.NOT_STARTED

  fun CriteriaCheck?.getSuitabilityStatus() =
    this?.let {
      if (it.criteriaMet) SUITABLE else UNSUITABLE
    } ?: NOT_STARTED

  fun CriteriaCheck?.getAnswer(questionName: String) =
    this?.let { questionAnswers[questionName] }
}
