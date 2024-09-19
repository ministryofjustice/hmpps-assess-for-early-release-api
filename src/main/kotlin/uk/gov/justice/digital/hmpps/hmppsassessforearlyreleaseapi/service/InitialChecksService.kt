package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.InitialChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.EligibilityCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.SuitabilityCheck

private fun List<EligibilityCheckDetails>.toStatus() = when {
  all { it.status == ELIGIBLE } -> ELIGIBLE
  any { it.status == INELIGIBLE } -> INELIGIBLE
  any { it.status == ELIGIBLE || it.status == IN_PROGRESS } -> IN_PROGRESS
  else -> NOT_STARTED
}

private fun List<SuitabilityCheckDetails>.toStatus() = when {
  all { it.status == SUITABLE } -> SUITABLE
  any { it.status == UNSUITABLE } -> UNSUITABLE
  any { it.status == UNSUITABLE || it.status == SuitabilityStatus.IN_PROGRESS } -> SuitabilityStatus.IN_PROGRESS
  else -> SuitabilityStatus.NOT_STARTED
}

@Service
class InitialChecksService(val offenderService: OffenderService, val policyService: PolicyService) {

  @Transactional
  fun getCurrentInitialChecks(prisonNumber: String): InitialChecks {
    val currentAssessment = offenderService.getCurrentAssessment(prisonNumber)
    val policy = policyService.getVersionFromPolicy(currentAssessment.policyVersion!!)

    val eligibilityDetails = policy.eligibilityCriteria.map { it.toEligibilityCheckDetails() }
    val suitabilityDetails = policy.suitabilityCriteria.map { it.toSuitabilityCheckDetails() }

    return InitialChecks(
      assessmentSummary = currentAssessment,
      complete = isComplete(eligibilityDetails, suitabilityDetails),
      checksPassed = isChecksPassed(eligibilityDetails, suitabilityDetails),
      eligibility = eligibilityDetails,
      eligibilityStatus = eligibilityDetails.toStatus(),
      suitability = suitabilityDetails,
      suitabilityStatus = suitabilityDetails.toStatus(),
    )
  }

  fun isComplete(
    eligibilityDetails: List<EligibilityCheckDetails>,
    suitabilityDetails: List<SuitabilityCheckDetails>,
  ): Boolean {
    val ineligible = eligibilityDetails.any { it.status == INELIGIBLE }
    val eligible = eligibilityDetails.all { it.status == ELIGIBLE }
    val suitabilityComplete = suitabilityDetails.all { it.status == SUITABLE || it.status == UNSUITABLE }

    val complete = (eligible && suitabilityComplete) || ineligible
    return complete
  }

  fun isChecksPassed(
    eligibilityDetails: List<EligibilityCheckDetails>,
    suitabilityDetails: List<SuitabilityCheckDetails>,
  ): Boolean {
    val eligible = eligibilityDetails.all { it.status == ELIGIBLE }
    val suitabilityPassed = suitabilityDetails.all { it.status == SUITABLE }
    val checksPassed = (eligible && suitabilityPassed)
    return checksPassed
  }
}

private fun SuitabilityCheck.toSuitabilityCheckDetails() =
  when {
    this is SuitabilityCheck.YesNo -> SuitabilityCheckDetails(
      code = code,
      taskName = name,
      question = question,
      status = SuitabilityStatus.NOT_STARTED,
      answer = null,
    )

    else -> error("unable to convert class of type ${this.javaClass}")
  }

private fun EligibilityCheck.toEligibilityCheckDetails() =
  when {
    this is EligibilityCheck.YesNo -> EligibilityCheckDetails(
      code = code,
      taskName = name,
      question = question,
      status = NOT_STARTED,
      answer = null,
    )

    else -> error("unable to convert class of type ${this.javaClass}")
  }
