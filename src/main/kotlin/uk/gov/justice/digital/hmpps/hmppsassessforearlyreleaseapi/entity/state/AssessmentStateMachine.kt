package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

import com.tinder.StateMachine
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.Approve
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.CompleteAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityChecksFailed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.EligibilityChecksPassed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.FailAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.OptBackIn
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.OptOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.Postpone
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.Refuse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.ReleaseOnHDC
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.ResidentialCheckStatusAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.StartAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.SubmitForAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.SubmitForDecision
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.Timeout
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AddressAndRiskChecksInProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AddressUnsuitable
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.Approved
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AwaitingAddressAndRiskChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AwaitingDecision
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AwaitingPreDecisionChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AwaitingPreReleaseChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.AwaitingRefusal
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.EligibilityAndSuitabilityInProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.EligibleAndSuitable
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.IneligibleOrUnsuitable
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.NotStarted
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.OptedOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.PassedPreReleaseChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.Postponed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.Refused
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.ReleasedOnHDC
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState.TimedOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.Companion.toState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.SideEffect.Error
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus

val assessmentStateMachine = StateMachine.create<AssessmentState, AssessmentLifecycleEvent, SideEffect> {
  initialState(NotStarted)

  state<NotStarted> {
    on<EligibilityAnswerProvided> { transitionTo(EligibilityAndSuitabilityInProgress) }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<EligibilityChecksPassed> { dontTransition(Error("Unable to transition to Eligible from ${this.status} directly")) }
    on<Timeout> { transitionTo(TimedOut) }
  }

  state<EligibilityAndSuitabilityInProgress> {
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { transitionTo(EligibleAndSuitable) }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Timeout> { transitionTo(TimedOut) }
  }

  state<EligibleAndSuitable> {
    on<EligibilityAnswerProvided> { transitionTo(EligibilityAndSuitabilityInProgress) }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<SubmitForAddressChecks> { transitionTo(AwaitingAddressAndRiskChecks) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
    on<Timeout> { transitionTo(TimedOut) }
  }

  state<AwaitingAddressAndRiskChecks> {
    on<ResidentialCheckStatusAnswerProvided> {
      when (it.checkStatus) {
        ResidentialChecksStatus.UNSUITABLE -> transitionTo(AddressAndRiskChecksInProgress)
        ResidentialChecksStatus.IN_PROGRESS -> transitionTo(AddressAndRiskChecksInProgress)
        ResidentialChecksStatus.SUITABLE -> dontTransition(Error("Unable to transition to suitable from ${this.status} directly"))
        else -> error("Unexpected eligibility status: $it")
      }
    }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<AddressAndRiskChecksInProgress> {
    on<ResidentialCheckStatusAnswerProvided> {
      when (it.checkStatus) {
        ResidentialChecksStatus.UNSUITABLE -> dontTransition()
        ResidentialChecksStatus.IN_PROGRESS -> dontTransition()
        ResidentialChecksStatus.SUITABLE -> dontTransition()
        else -> error("Unexpected eligibility status: $it")
      }
    }
    on<CompleteAddressChecks> { transitionTo(AwaitingPreDecisionChecks) }
    on<FailAddressChecks> { transitionTo(AddressUnsuitable) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<AwaitingPreDecisionChecks> {
    on<SubmitForDecision> { transitionTo(AwaitingDecision) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Postpone> { transitionTo(Postponed) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<AwaitingDecision> {
    on<Approve> { transitionTo(Approved) }
    on<Refuse> { transitionTo(Refused) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Postpone> { transitionTo(Postponed) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<Approved> {
    on<SubmitForAddressChecks> { transitionTo(AwaitingPreReleaseChecks) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Postpone> { transitionTo(Postponed) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<AwaitingPreReleaseChecks> {
    on<CompleteAddressChecks> { transitionTo(PassedPreReleaseChecks) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Postpone> { transitionTo(Postponed) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<PassedPreReleaseChecks> {
    on<ReleaseOnHDC> { transitionTo(ReleasedOnHDC) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Postpone> { transitionTo(Postponed) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<AddressUnsuitable> {
    on<SubmitForAddressChecks> { transitionTo(AwaitingRefusal) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<AwaitingRefusal> {
    on<Timeout> { transitionTo(TimedOut) }
    on<Refuse> { transitionTo(Refused) }
    on<EligibilityAnswerProvided> { dontTransition() }
    on<EligibilityChecksPassed> { dontTransition() }
    on<EligibilityChecksFailed> { transitionTo(IneligibleOrUnsuitable) }
    on<SubmitForAddressChecks> { transitionTo(AwaitingAddressAndRiskChecks) }
  }

  state<IneligibleOrUnsuitable> {
    on<EligibilityAnswerProvided> { transitionTo(EligibilityAndSuitabilityInProgress) }
    on<EligibilityChecksPassed> { transitionTo(EligibleAndSuitable) }
    on<EligibilityChecksFailed> { dontTransition() }
    on<SubmitForAddressChecks> { transitionTo(AwaitingAddressAndRiskChecks) }
    on<StartAddressChecks> { transitionTo(AddressAndRiskChecksInProgress) }
    on<SubmitForDecision> { transitionTo(AwaitingPreDecisionChecks) }
    on<Approve> { transitionTo(Approved) }
    on<CompleteAddressChecks> { transitionTo(AwaitingPreReleaseChecks) }
    on<ReleaseOnHDC> { transitionTo(PassedPreReleaseChecks) }
    on<FailAddressChecks> { transitionTo(AddressUnsuitable) }
    on<Refuse> { transitionTo(Refused) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
    on<Timeout> { transitionTo(TimedOut) }
  }

  state<Refused> {
    on<Timeout> { transitionTo(TimedOut) }
  }

  state<TimedOut> {
    on<SubmitForAddressChecks> { transitionTo(AwaitingAddressAndRiskChecks) }
    on<StartAddressChecks> { transitionTo(AddressAndRiskChecksInProgress) }
    on<SubmitForDecision> { transitionTo(AwaitingPreDecisionChecks) }
    on<Approve> { transitionTo(Approved) }
    on<CompleteAddressChecks> { transitionTo(AwaitingPreReleaseChecks) }
    on<ReleaseOnHDC> { transitionTo(PassedPreReleaseChecks) }
    on<FailAddressChecks> { transitionTo(AddressUnsuitable) }
    on<Refuse> { transitionTo(Refused) }
    on<OptOut> { transitionTo(OptedOut(this.status)) }
  }

  state<Postponed> {
    on<SubmitForDecision> { transitionTo(AwaitingPreDecisionChecks) }
    on<Approve> { transitionTo(Approved) }
    on<CompleteAddressChecks> { transitionTo(AwaitingPreReleaseChecks) }
    on<ReleaseOnHDC> { transitionTo(PassedPreReleaseChecks) }
    on<Timeout> { transitionTo(TimedOut) }
  }

  state<OptedOut> {
    on<SubmitForAddressChecks> { transitionTo(AwaitingAddressAndRiskChecks) }
    on<StartAddressChecks> { transitionTo(AddressAndRiskChecksInProgress) }
    on<SubmitForDecision> { transitionTo(AwaitingPreDecisionChecks) }
    on<Approve> { transitionTo(Approved) }
    on<CompleteAddressChecks> { transitionTo(AwaitingPreReleaseChecks) }
    on<ReleaseOnHDC> { transitionTo(PassedPreReleaseChecks) }
    on<FailAddressChecks> { transitionTo(AddressUnsuitable) }
    on<Refuse> { transitionTo(Refused) }
    on<Timeout> { transitionTo(TimedOut) }
    on<OptBackIn> { transitionTo(this.previousStatus.toState()) }
  }

  state<ReleasedOnHDC> {}
}

sealed interface SideEffect {
  data class Error(val message: String) : SideEffect
}
