package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PREPARE_FOR_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.COMPLETE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.READY_TO_START

enum class AssessmentStatus {
  NOT_STARTED {
    override fun transitions() = transitions(
      ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
      TIMED_OUT,
    )

    override fun tasks() = setOf(
      TaskProgress.Fixed(ASSESS_ELIGIBILITY, READY_TO_START),
      TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
      TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
      TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
      TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
    )
  },

  ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS {
    override fun transitions() = transitions(
      ELIGIBLE_AND_SUITABLE,
      INELIGIBLE_OR_UNSUITABLE,
      TIMED_OUT,
    )

    override fun tasks() = setOf(
      TaskProgress.Fixed(ASSESS_ELIGIBILITY, IN_PROGRESS),
      TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
      TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
      TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
      TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
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

    override fun tasks() = setOf(
      TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
      TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, READY_TO_START),
      TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
      TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
      TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
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

    override fun tasks() = setOf(
      TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
      TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
      TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
      TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
      TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
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

    override fun tasks() = setOf(
      TaskProgress.Fixed(ASSESS_ELIGIBILITY, LOCKED),
      TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, IN_PROGRESS),
      TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
      TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
      TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
    )
  },

  RELEASED_ON_HDC,

  ;

  fun allowsTransitionTo(state: AssessmentStatus): Boolean = transitions().map { it.to }.contains(state)

  open fun transitions(): Set<Transition> = emptySet()

  open fun tasks(): Set<TaskProgress> = emptySet()
}

fun transitions(vararg states: AssessmentStatus) = states.map { Transition(to = it) }.toSet()

data class Transition(val to: AssessmentStatus)

sealed interface TaskProgress {
  val task: Task
  val status: (assessment: Assessment) -> TaskStatus

  class Fixed(override val task: Task, status: TaskStatus) : TaskProgress {
    override val status: (assessment: Assessment) -> TaskStatus = { status }
  }
}
