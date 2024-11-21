package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import com.tinder.StateMachine
import jakarta.persistence.Embeddable
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CREATE_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.MAKE_A_RISK_MANAGEMENT_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PREPARE_FOR_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.SEND_CHECKS_TO_PRISON
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.COMPLETE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PROBATION_COM

enum class AssessmentStatus {
  NOT_STARTED {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, READY_TO_START),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
    )
  },

  ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, IN_PROGRESS),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
    )
  },

  ELIGIBLE_AND_SUITABLE {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, READY_TO_START),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
    )
  },

  AWAITING_ADDRESS_AND_RISK_CHECKS {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, COMPLETE),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
      PROBATION_COM to listOf(
        TaskProgress.Fixed(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION, READY_TO_START),
        TaskProgress.Fixed(MAKE_A_RISK_MANAGEMENT_DECISION, LOCKED),
        TaskProgress.Fixed(SEND_CHECKS_TO_PRISON, LOCKED),
        TaskProgress.Fixed(CREATE_LICENCE, LOCKED),
      ),
    )
  },

  ADDRESS_AND_RISK_CHECKS_IN_PROGRESS {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, COMPLETE),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
      PROBATION_COM to listOf(
        TaskProgress.Fixed(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION, IN_PROGRESS),
        TaskProgress.Fixed(MAKE_A_RISK_MANAGEMENT_DECISION, LOCKED),
        TaskProgress.Fixed(SEND_CHECKS_TO_PRISON, LOCKED),
        TaskProgress.Fixed(CREATE_LICENCE, LOCKED),
      ),
    )
  },

  AWAITING_PRE_DECISION_CHECKS,

  AWAITING_DECISION,

  APPROVED,

  AWAITING_PRE_RELEASE_CHECKS,

  PASSED_PRE_RELEASE_CHECKS,

  ADDRESS_UNSUITABLE,

  AWAITING_REFUSAL,

  INELIGIBLE_OR_UNSUITABLE {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
    )
  },

  REFUSED,
  TIMED_OUT,

  POSTPONED,

  OPTED_OUT {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, LOCKED),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, IN_PROGRESS),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(PREPARE_FOR_RELEASE, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
    )
  },

  RELEASED_ON_HDC,

  ;

  open fun tasks(): Map<UserRole, List<TaskProgress>> = emptyMap()
}

@Embeddable
sealed interface AssessmentState {
  val label: AssessmentStatus

  object NotStarted : AssessmentState {
    override val label = AssessmentStatus.NOT_STARTED
  }

  object EligibilityAndSuitabilityInProgress : AssessmentState {
    override val label = AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
  }

  object EligibleAndSuitable : AssessmentState {
    override val label = AssessmentStatus.ELIGIBLE_AND_SUITABLE
  }

  object AwaitingAddressAndRiskChecks : AssessmentState {
    override val label = AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
  }

  object AddressAndRiskChecksInProgress : AssessmentState {
    override val label = AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
  }

  object AwaitingPreDecisionChecks : AssessmentState {
    override val label = AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
  }

  object AwaitingDecision : AssessmentState {
    override val label = AssessmentStatus.AWAITING_DECISION
  }

  object Approved : AssessmentState {
    override val label = AssessmentStatus.APPROVED
  }

  object AwaitingPreReleaseChecks : AssessmentState {
    override val label = AssessmentStatus.AWAITING_PRE_RELEASE_CHECKS
  }

  object PassedPreReleaseChecks : AssessmentState {
    override val label = AssessmentStatus.PASSED_PRE_RELEASE_CHECKS
  }

  object AddressUnsuitable : AssessmentState {
    override val label = AssessmentStatus.ADDRESS_UNSUITABLE
  }

  object AwaitingRefusal : AssessmentState {
    override val label = AssessmentStatus.AWAITING_REFUSAL
  }

  object IneligibleOrUnsuitable : AssessmentState {
    override val label = AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
  }

  object Refused : AssessmentState {
    override val label = AssessmentStatus.REFUSED
  }

  object TimedOut : AssessmentState {
    override val label = AssessmentStatus.TIMED_OUT
  }

  object Postponed : AssessmentState {
    override val label = AssessmentStatus.POSTPONED
  }

  data class OptedOut(val previousState: AssessmentState) : AssessmentState {
    override val label = AssessmentStatus.OPTED_OUT
  }

  object ReleasedOnHDC : AssessmentState {
    override val label = AssessmentStatus.RELEASED_ON_HDC
  }
}

sealed class AssessmentLifecycleEvent {
  object StartEligibilityAndSuitability : AssessmentLifecycleEvent()
  object CompleteEligibilityAndSuitability : AssessmentLifecycleEvent()
  object FailEligibilityAndSuitability : AssessmentLifecycleEvent()
  object SubmitForAddressChecks : AssessmentLifecycleEvent()
  object StartAddressChecks : AssessmentLifecycleEvent()
  object CompleteAddressChecks : AssessmentLifecycleEvent()
  object FailAddressChecks : AssessmentLifecycleEvent()
  object SubmitForDecision : AssessmentLifecycleEvent()
  object Approve : AssessmentLifecycleEvent()
  object Refuse : AssessmentLifecycleEvent()
  object OptOut : AssessmentLifecycleEvent()
  object OptBackIn : AssessmentLifecycleEvent()
  object Timeout : AssessmentLifecycleEvent()
  object Postpone : AssessmentLifecycleEvent()
  object ReleaseOnHDC : AssessmentLifecycleEvent()
}

val assessmentStateMachine = StateMachine.create<AssessmentState, AssessmentLifecycleEvent, Unit> {
  initialState(AssessmentState.NotStarted)

  state<AssessmentState.NotStarted> {
    on<AssessmentLifecycleEvent.StartEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibilityAndSuitabilityInProgress)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
  }

  state<AssessmentState.EligibilityAndSuitabilityInProgress> {
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
  }

  state<AssessmentState.EligibleAndSuitable> {
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingAddressAndRiskChecks)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
  }

  state<AssessmentState.AwaitingAddressAndRiskChecks> {
    on<AssessmentLifecycleEvent.StartAddressChecks> {
      transitionTo(AssessmentState.AddressAndRiskChecksInProgress)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.AddressAndRiskChecksInProgress> {
    on<AssessmentLifecycleEvent.CompleteAddressChecks> {
      transitionTo(AssessmentState.AwaitingPreDecisionChecks)
    }
    on<AssessmentLifecycleEvent.FailAddressChecks> {
      transitionTo(AssessmentState.AddressUnsuitable)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.AwaitingPreDecisionChecks> {
    on<AssessmentLifecycleEvent.SubmitForDecision> {
      transitionTo(AssessmentState.AwaitingDecision)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Postpone> {
      transitionTo(AssessmentState.Postponed)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.AwaitingDecision> {
    on<AssessmentLifecycleEvent.Approve> {
      transitionTo(AssessmentState.Approved)
    }
    on<AssessmentLifecycleEvent.Refuse> {
      transitionTo(AssessmentState.Refused)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Postpone> {
      transitionTo(AssessmentState.Postponed)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.Approved> {
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Postpone> {
      transitionTo(AssessmentState.Postponed)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.AwaitingPreReleaseChecks> {
    on<AssessmentLifecycleEvent.CompleteAddressChecks> {
      transitionTo(AssessmentState.PassedPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Postpone> {
      transitionTo(AssessmentState.Postponed)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.PassedPreReleaseChecks> {
    on<AssessmentLifecycleEvent.ReleaseOnHDC> {
      transitionTo(AssessmentState.ReleasedOnHDC)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Postpone> {
      transitionTo(AssessmentState.Postponed)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.AddressUnsuitable> {
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingRefusal)
    }
    on<AssessmentLifecycleEvent.FailEligibilityAndSuitability> {
      transitionTo(AssessmentState.IneligibleOrUnsuitable)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.AwaitingRefusal> {
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.Refuse> {
      transitionTo(AssessmentState.Refused)
    }
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingAddressAndRiskChecks)
    }
  }

  state<AssessmentState.IneligibleOrUnsuitable> {
    on<AssessmentLifecycleEvent.StartEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibilityAndSuitabilityInProgress)
    }
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingAddressAndRiskChecks)
    }
    on<AssessmentLifecycleEvent.StartAddressChecks> {
      transitionTo(AssessmentState.AddressAndRiskChecksInProgress)
    }
    on<AssessmentLifecycleEvent.SubmitForDecision> {
      transitionTo(AssessmentState.AwaitingPreDecisionChecks)
    }
    on<AssessmentLifecycleEvent.Approve> {
      transitionTo(AssessmentState.Approved)
    }
    on<AssessmentLifecycleEvent.CompleteAddressChecks> {
      transitionTo(AssessmentState.AwaitingPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.ReleaseOnHDC> {
      transitionTo(AssessmentState.PassedPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.FailAddressChecks> {
      transitionTo(AssessmentState.AddressUnsuitable)
    }
    on<AssessmentLifecycleEvent.Refuse> {
      transitionTo(AssessmentState.Refused)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
  }

  state<AssessmentState.Refused> {
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
  }

  state<AssessmentState.TimedOut> {
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingAddressAndRiskChecks)
    }
    on<AssessmentLifecycleEvent.StartAddressChecks> {
      transitionTo(AssessmentState.AddressAndRiskChecksInProgress)
    }
    on<AssessmentLifecycleEvent.SubmitForDecision> {
      transitionTo(AssessmentState.AwaitingPreDecisionChecks)
    }
    on<AssessmentLifecycleEvent.Approve> {
      transitionTo(AssessmentState.Approved)
    }
    on<AssessmentLifecycleEvent.CompleteAddressChecks> {
      transitionTo(AssessmentState.AwaitingPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.ReleaseOnHDC> {
      transitionTo(AssessmentState.PassedPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.FailAddressChecks> {
      transitionTo(AssessmentState.AddressUnsuitable)
    }
    on<AssessmentLifecycleEvent.Refuse> {
      transitionTo(AssessmentState.Refused)
    }
    on<AssessmentLifecycleEvent.OptOut> {
      transitionTo(AssessmentState.OptedOut(this))
    }
  }

  state<AssessmentState.Postponed> {
    on<AssessmentLifecycleEvent.SubmitForDecision> {
      transitionTo(AssessmentState.AwaitingPreDecisionChecks)
    }
    on<AssessmentLifecycleEvent.Approve> {
      transitionTo(AssessmentState.Approved)
    }
    on<AssessmentLifecycleEvent.CompleteAddressChecks> {
      transitionTo(AssessmentState.AwaitingPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.ReleaseOnHDC> {
      transitionTo(AssessmentState.PassedPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
  }

  state<AssessmentState.OptedOut> {
    on<AssessmentLifecycleEvent.CompleteEligibilityAndSuitability> {
      transitionTo(AssessmentState.EligibleAndSuitable)
    }
    on<AssessmentLifecycleEvent.SubmitForAddressChecks> {
      transitionTo(AssessmentState.AwaitingAddressAndRiskChecks)
    }
    on<AssessmentLifecycleEvent.StartAddressChecks> {
      transitionTo(AssessmentState.AddressAndRiskChecksInProgress)
    }
    on<AssessmentLifecycleEvent.SubmitForDecision> {
      transitionTo(AssessmentState.AwaitingPreDecisionChecks)
    }
    on<AssessmentLifecycleEvent.Approve> {
      transitionTo(AssessmentState.Approved)
    }
    on<AssessmentLifecycleEvent.CompleteAddressChecks> {
      transitionTo(AssessmentState.AwaitingPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.ReleaseOnHDC> {
      transitionTo(AssessmentState.PassedPreReleaseChecks)
    }
    on<AssessmentLifecycleEvent.FailAddressChecks> {
      transitionTo(AssessmentState.AddressUnsuitable)
    }
    on<AssessmentLifecycleEvent.Refuse> {
      transitionTo(AssessmentState.Refused)
    }
    on<AssessmentLifecycleEvent.Timeout> {
      transitionTo(AssessmentState.TimedOut)
    }
    on<AssessmentLifecycleEvent.OptBackIn> {
      transitionTo(this.previousState)
    }
  }

  state<AssessmentState.ReleasedOnHDC> {}
}

sealed interface TaskProgress {
  val task: Task
  val status: (assessment: Assessment) -> TaskStatus

  class Fixed(override val task: Task, status: TaskStatus) : TaskProgress {
    override val status: (assessment: Assessment) -> TaskStatus = { status }
  }
}
