package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

enum class AssessmentStatus {
  NOT_STARTED,
  ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
  ELIGIBLE_AND_SUITABLE,
  AWAITING_ADDRESS_AND_RISK_CHECKS,
  ADDRESS_AND_RISK_CHECKS_IN_PROGRESS,
  AWAITING_PRE_DECISION_CHECKS,
  AWAITING_DECISION,
  APPROVED,
  AWAITING_PRE_RELEASE_CHECKS,
  PASSED_PRE_RELEASE_CHECKS,
  ADDRESS_UNSUITABLE,
  AWAITING_REFUSAL,
  INELIGIBLE_OR_UNSUITABLE,
  REFUSED,
  TIMED_OUT,
  POSTPONED,
  OPTED_OUT,
  RELEASED_ON_HDC,
  ;

  fun isOneOf(vararg statuses: AssessmentStatus) = statuses.any { status -> this == status }

  companion object {
    // Note: ideally we'd be able to unmarshall this directly from the stored Assessment entity, this is a workaround.
    fun AssessmentStatus.toState(previousStatus: AssessmentStatus? = null): AssessmentState = when (this) {
      NOT_STARTED -> AssessmentState.NotStarted
      ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS -> AssessmentState.EligibilityAndSuitabilityInProgress
      ELIGIBLE_AND_SUITABLE -> AssessmentState.EligibleAndSuitable
      AWAITING_ADDRESS_AND_RISK_CHECKS -> AssessmentState.AwaitingAddressAndRiskChecks
      ADDRESS_AND_RISK_CHECKS_IN_PROGRESS -> AssessmentState.AddressAndRiskChecksInProgress
      AWAITING_PRE_DECISION_CHECKS -> AssessmentState.AwaitingPreDecisionChecks
      AWAITING_DECISION -> AssessmentState.AwaitingDecision
      APPROVED -> AssessmentState.Approved
      AWAITING_PRE_RELEASE_CHECKS -> AssessmentState.AwaitingPreReleaseChecks
      PASSED_PRE_RELEASE_CHECKS -> AssessmentState.PassedPreReleaseChecks
      ADDRESS_UNSUITABLE -> AssessmentState.AddressUnsuitable
      AWAITING_REFUSAL -> AssessmentState.AwaitingRefusal
      INELIGIBLE_OR_UNSUITABLE -> AssessmentState.IneligibleOrUnsuitable
      REFUSED -> AssessmentState.Refused
      TIMED_OUT -> AssessmentState.TimedOut
      POSTPONED -> AssessmentState.Postponed
      OPTED_OUT -> AssessmentState.OptedOut(
        previousStatus ?: error("No previous status available, current status is $this"),
      )
      RELEASED_ON_HDC -> AssessmentState.ReleasedOnHDC
    }

    fun inFlightStatuses(): List<AssessmentStatus> = entries.filter { it != TIMED_OUT && it != REFUSED && it != RELEASED_ON_HDC }
  }
}
