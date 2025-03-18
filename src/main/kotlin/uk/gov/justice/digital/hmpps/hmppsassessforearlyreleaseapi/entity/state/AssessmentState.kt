package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

abstract class AssessmentState {
  abstract val status: AssessmentStatus

  object NotStarted : AssessmentState() {
    override val status: AssessmentStatus
      get() = AssessmentStatus.NOT_STARTED
  }

  object EligibilityAndSuitabilityInProgress : AssessmentState() {
    override val status = AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
  }

  object EligibleAndSuitable : AssessmentState() {
    override val status = AssessmentStatus.ELIGIBLE_AND_SUITABLE
  }

  object AwaitingAddressAndRiskChecks : AssessmentState() {
    override val status = AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
  }

  object AddressAndRiskChecksInProgress : AssessmentState() {
    override val status = AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
  }

  object AwaitingPreDecisionChecks : AssessmentState() {
    override val status = AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
  }

  object AwaitingDecision : AssessmentState() {
    override val status = AssessmentStatus.AWAITING_DECISION
  }

  object Approved : AssessmentState() {
    override val status = AssessmentStatus.APPROVED
  }

  object AwaitingPreReleaseChecks : AssessmentState() {
    override val status = AssessmentStatus.AWAITING_PRE_RELEASE_CHECKS
  }

  object PassedPreReleaseChecks : AssessmentState() {
    override val status = AssessmentStatus.PASSED_PRE_RELEASE_CHECKS
  }

  object AddressUnsuitable : AssessmentState() {
    override val status = AssessmentStatus.ADDRESS_UNSUITABLE
  }

  object AwaitingRefusal : AssessmentState() {
    override val status = AssessmentStatus.AWAITING_REFUSAL
  }

  object IneligibleOrUnsuitable : AssessmentState() {
    override val status = AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
  }

  object Refused : AssessmentState() {
    override val status = AssessmentStatus.REFUSED
  }

  object TimedOut : AssessmentState() {
    override val status = AssessmentStatus.TIMED_OUT
  }

  object Postponed : AssessmentState() {
    override val status = AssessmentStatus.POSTPONED
  }

  data class OptedOut(val previousStatus: AssessmentStatus) : AssessmentState() {
    override val status = AssessmentStatus.OPTED_OUT
  }

  object ReleasedOnHDC : AssessmentState() {
    override val status = AssessmentStatus.RELEASED_ON_HDC
  }
}
