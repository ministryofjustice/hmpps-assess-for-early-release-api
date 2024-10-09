package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

enum class AssessmentStatus {
  NOT_STARTED {
    override fun transitions() = transitions(
      ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
      TIMED_OUT,
    )
  },

  ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS {
    override fun transitions() = transitions(
      ELIGIBLE_AND_SUITABLE,
      INELIGIBLE_OR_UNSUITABLE,
      TIMED_OUT,
    )
  },

  ELIGIBLE_AND_SUITABLE {
    override fun transitions() =
      transitions(
        AWAITING_ADDRESS_AND_RISK_CHECKS,
        INELIGIBLE_OR_UNSUITABLE,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  AWAITING_ADDRESS_AND_RISK_CHECKS {
    override fun transitions() =
      transitions(
        ADDRESS_AND_RISK_CHECKS_IN_PROGRESS,
        INELIGIBLE_OR_UNSUITABLE,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  ADDRESS_AND_RISK_CHECKS_IN_PROGRESS {
    override fun transitions() =
      transitions(
        AWAITING_PRE_DECISION_CHECKS,
        ADDRESS_UNSUITABLE,
        INELIGIBLE_OR_UNSUITABLE,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  AWAITING_PRE_DECISION_CHECKS {
    override fun transitions() =
      transitions(
        AWAITING_DECISION,
        INELIGIBLE_OR_UNSUITABLE,
        POSTPONED,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  AWAITING_DECISION {
    override fun transitions() =
      transitions(
        APPROVED,
        REFUSED,
        INELIGIBLE_OR_UNSUITABLE,
        POSTPONED,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  APPROVED {
    override fun transitions() =
      transitions(
        AWAITING_PRE_RELEASE_CHECKS,
        INELIGIBLE_OR_UNSUITABLE,
        POSTPONED,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  AWAITING_PRE_RELEASE_CHECKS {
    override fun transitions() =
      transitions(
        PASSED_PRE_RELEASE_CHECKS,
        INELIGIBLE_OR_UNSUITABLE,
        POSTPONED,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  PASSED_PRE_RELEASE_CHECKS {
    override fun transitions() =
      transitions(
        RELEASED_ON_HDC,
        INELIGIBLE_OR_UNSUITABLE,
        POSTPONED,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  ADDRESS_UNSUITABLE {
    override fun transitions() =
      transitions(
        ELIGIBLE_AND_SUITABLE,
        AWAITING_REFUSAL,
        INELIGIBLE_OR_UNSUITABLE,
        TIMED_OUT,
        OPTED_OUT,
      )
  },

  AWAITING_REFUSAL {
    override fun transitions() =
      transitions(
        TIMED_OUT,
        REFUSED,
        ELIGIBLE_AND_SUITABLE,
        AWAITING_ADDRESS_AND_RISK_CHECKS,
      )
  },

  INELIGIBLE_OR_UNSUITABLE {
    override fun transitions() =
      transitions(
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
        REFUSED,
        OPTED_OUT,
        TIMED_OUT,
      )
  },

  REFUSED {
    override fun transitions() =
      transitions(
        ELIGIBLE_AND_SUITABLE,
        TIMED_OUT,
      )
  },
  TIMED_OUT {
    override fun transitions() = (AssessmentStatus.values().toSet() - RELEASED_ON_HDC).map { Transition(it) }.toSet()
  },

  POSTPONED {
    override fun transitions() =
      transitions(
        AWAITING_PRE_DECISION_CHECKS,
        AWAITING_DECISION,
        APPROVED,
        AWAITING_PRE_RELEASE_CHECKS,
        PASSED_PRE_RELEASE_CHECKS,
        TIMED_OUT,
      )
  },

  OPTED_OUT {
    override fun transitions() =
      transitions(
        ELIGIBLE_AND_SUITABLE,
        AWAITING_ADDRESS_AND_RISK_CHECKS,
        ADDRESS_AND_RISK_CHECKS_IN_PROGRESS,
        AWAITING_PRE_DECISION_CHECKS,
        AWAITING_DECISION,
        APPROVED,
        AWAITING_PRE_RELEASE_CHECKS,
        PASSED_PRE_RELEASE_CHECKS,
        ADDRESS_UNSUITABLE,
        TIMED_OUT,
      )
  },

  RELEASED_ON_HDC,

  ;

  open fun transitions(): Set<Transition> = emptySet()
}

fun transitions(vararg states: AssessmentStatus) = states.map { Transition(to = it) }.toSet()

data class Transition(val to: AssessmentStatus)
