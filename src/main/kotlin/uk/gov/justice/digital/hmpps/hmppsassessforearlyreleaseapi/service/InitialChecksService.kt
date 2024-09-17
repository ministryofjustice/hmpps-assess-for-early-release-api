package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.InitialChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.EligibilityCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.SuitabilityCheck

@Service
class InitialChecksService(val offenderService: OffenderService, val policyService: PolicyService) {

  @Transactional
  fun getCurrentInitialChecks(prisonNumber: String): InitialChecks {
    val currentAssessment = offenderService.getCurrentAssessment(prisonNumber)
    val policy = policyService.getVersionFromPolicy(currentAssessment.policyVersion!!)
    return InitialChecks(
      assessmentSummary = currentAssessment,
      eligibility = policy.eligibilityCriteria.map { it.toEligibilityCheckDetails() },
      suitability = policy.suitabilityCriteria.map { it.toSuitabilityCheckDetails() },
    )
  }
}

private fun SuitabilityCheck.toSuitabilityCheckDetails() =
  when {
    this is SuitabilityCheck.YesNo -> SuitabilityCheckDetails(
      code = code,
      taskName = name,
      question = question,
      state = SuitabilityState.NOT_STARTED,
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
      state = EligibilityState.NOT_STARTED,
      answer = null,
    )

    else -> error("unable to convert class of type ${this.javaClass}")
  }
