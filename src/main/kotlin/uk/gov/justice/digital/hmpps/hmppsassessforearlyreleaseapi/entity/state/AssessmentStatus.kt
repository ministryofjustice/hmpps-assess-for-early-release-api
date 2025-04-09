package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.APPROVE_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.COMPLETE_14_DAY_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.COMPLETE_2_DAY_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CONFIRM_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CONSULT_THE_VLO_AND_POM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.CREATE_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.MAKE_A_RISK_MANAGEMENT_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.OPT_IN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.SEND_CHECKS_TO_PRISON
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.COMPLETE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_DM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PROBATION_COM

enum class AssessmentStatus {
  NOT_STARTED {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, READY_TO_START),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
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
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
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
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
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
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
      PROBATION_COM to listOf(
        TaskProgress.Dynamic(
          CONSULT_THE_VLO_AND_POM,
        ) {
          if (it.victimContactSchemeOptedIn == true) COMPLETE else READY_TO_START
        },
        TaskProgress.Dynamic(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION) {
          if (it.victimContactSchemeOptedIn == true) READY_TO_START else LOCKED
        },
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
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
      PROBATION_COM to listOf(
        TaskProgress.Fixed(CONSULT_THE_VLO_AND_POM, COMPLETE),
        TaskProgress.Fixed(CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION, IN_PROGRESS),
        TaskProgress.Fixed(MAKE_A_RISK_MANAGEMENT_DECISION, LOCKED),
        TaskProgress.Dynamic(
          SEND_CHECKS_TO_PRISON,
        ) {
          if (it.addressChecksComplete) READY_TO_START else LOCKED
        },
        TaskProgress.Fixed(CREATE_LICENCE, LOCKED),
      ),
    )
  },

  AWAITING_PRE_DECISION_CHECKS {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, READY_TO_START),
      ),
    )
  },

  AWAITING_DECISION {
    override fun tasks() = mapOf(
      PRISON_DM to listOf(
        TaskProgress.Fixed(CONFIRM_RELEASE, READY_TO_START),
        TaskProgress.Fixed(APPROVE_LICENCE, LOCKED),
      ),
    )
  },

  APPROVED {
    override fun tasks() = mapOf(
      PRISON_DM to listOf(
        TaskProgress.Fixed(APPROVE_LICENCE, READY_TO_START),
      ),
    )
  },

  AWAITING_PRE_RELEASE_CHECKS,

  PASSED_PRE_RELEASE_CHECKS,

  ADDRESS_UNSUITABLE,

  AWAITING_REFUSAL {
    override fun tasks() = mapOf(
      PRISON_DM to listOf(
        TaskProgress.Fixed(CONFIRM_RELEASE, READY_TO_START),
        TaskProgress.Fixed(APPROVE_LICENCE, LOCKED),
      ),
    )
  },

  INELIGIBLE_OR_UNSUITABLE {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, COMPLETE),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, LOCKED),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
    )
  },

  REFUSED,
  TIMED_OUT {
    override fun tasks() = mapOf(
      PRISON_DM to listOf(
        TaskProgress.Fixed(CONFIRM_RELEASE, LOCKED),
        TaskProgress.Fixed(APPROVE_LICENCE, LOCKED),
      ),
    )
  },

  POSTPONED,

  OPTED_OUT {
    override fun tasks() = mapOf(
      PRISON_CA to listOf(
        TaskProgress.Fixed(ASSESS_ELIGIBILITY, LOCKED),
        TaskProgress.Fixed(ENTER_CURFEW_ADDRESS, IN_PROGRESS),
        TaskProgress.Fixed(REVIEW_APPLICATION_AND_SEND_FOR_DECISION, LOCKED),
        TaskProgress.Fixed(COMPLETE_14_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(COMPLETE_2_DAY_CHECKS, LOCKED),
        TaskProgress.Fixed(PRINT_LICENCE, LOCKED),
      ),
      PRISON_DM to listOf(
        TaskProgress.Fixed(CONFIRM_RELEASE, LOCKED),
        TaskProgress.Fixed(APPROVE_LICENCE, LOCKED),
        TaskProgress.Fixed(OPT_IN, LOCKED),
      ),
    )
  },

  RELEASED_ON_HDC,
  ;

  open fun tasks(): Map<UserRole, List<TaskProgress>> = emptyMap()

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
