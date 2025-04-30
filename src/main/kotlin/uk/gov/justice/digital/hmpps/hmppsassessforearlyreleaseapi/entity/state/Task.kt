package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_DM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PROBATION_COM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.APPROVED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_REFUSAL
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.TIMED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.COMPLETE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus

enum class Task {

  ASSESS_ELIGIBILITY {
    override fun visibleForRoles() = setOf(PRISON_CA)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.eligibilityChecksStatus) {
      EligibilityStatus.NOT_STARTED -> READY_TO_START
      EligibilityStatus.IN_PROGRESS -> IN_PROGRESS
      else -> COMPLETE
    }
  },

  ENTER_CURFEW_ADDRESS {
    override fun visibleForRoles() = setOf(PRISON_CA)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      NOT_STARTED, ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, INELIGIBLE_OR_UNSUITABLE -> LOCKED
      ELIGIBLE_AND_SUITABLE -> READY_TO_START
      AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS -> COMPLETE
      OPTED_OUT -> IN_PROGRESS
      else -> null
    }
  },

  REVIEW_APPLICATION_AND_SEND_FOR_DECISION {
    override fun visibleForRoles() = setOf(PRISON_CA)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      NOT_STARTED, ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, ELIGIBLE_AND_SUITABLE, AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS, INELIGIBLE_OR_UNSUITABLE, OPTED_OUT -> LOCKED
      AWAITING_PRE_DECISION_CHECKS -> READY_TO_START
      else -> null
    }
  },

  COMPLETE_14_DAY_CHECKS {
    override fun visibleForRoles() = setOf(PRISON_CA)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      NOT_STARTED, ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, ELIGIBLE_AND_SUITABLE, AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS, INELIGIBLE_OR_UNSUITABLE, OPTED_OUT -> LOCKED
      else -> null
    }
  },

  COMPLETE_2_DAY_CHECKS {
    override fun visibleForRoles() = setOf(PRISON_CA)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      NOT_STARTED, ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, ELIGIBLE_AND_SUITABLE, AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS, INELIGIBLE_OR_UNSUITABLE, OPTED_OUT -> LOCKED
      else -> null
    }
  },

  PRINT_LICENCE {
    override fun visibleForRoles() = setOf(PRISON_CA)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      NOT_STARTED, ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS, ELIGIBLE_AND_SUITABLE, AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS, INELIGIBLE_OR_UNSUITABLE, OPTED_OUT -> LOCKED
      else -> null
    }
  },

  CONSULT_THE_VLO_AND_POM {
    override fun visibleForRoles() = setOf(PROBATION_COM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_ADDRESS_AND_RISK_CHECKS ->
        if (assessment.victimContactSchemeOptedIn != null) COMPLETE else READY_TO_START

      ADDRESS_AND_RISK_CHECKS_IN_PROGRESS -> COMPLETE
      else -> null
    }
  },

  CHECK_ADDRESSES_OR_COMMUNITY_ACCOMMODATION {
    override fun visibleForRoles() = setOf(PROBATION_COM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_ADDRESS_AND_RISK_CHECKS -> READY_TO_START
      ADDRESS_AND_RISK_CHECKS_IN_PROGRESS -> IN_PROGRESS
      else -> null
    }
  },

  RECORD_NON_DISCLOSABLE_INFORMATION {
    override fun visibleForRoles() = setOf(PROBATION_COM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS ->
        if (assessment.hasNonDisclosableInformation != null) COMPLETE else READY_TO_START

      else -> null
    }
  },

  MAKE_A_RISK_MANAGEMENT_DECISION {
    override fun visibleForRoles() = setOf(PROBATION_COM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS -> LOCKED
      else -> null
    }
  },

  SEND_CHECKS_TO_PRISON {
    override fun visibleForRoles() = setOf(PROBATION_COM)
    override fun taskStatus(assessment: Assessment): TaskStatus? {
      if (!assessment.status.isOneOf(AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)) {
        return null
      }
      return when {
        assessment.addressChecksStatus == ResidentialChecksStatus.UNSUITABLE -> READY_TO_START
        assessment.addressChecksStatus == ResidentialChecksStatus.SUITABLE && assessment.victimContactSchemeOptedIn != null && assessment.hasNonDisclosableInformation != null -> READY_TO_START
        else -> LOCKED
      }
    }
  },

  CREATE_LICENCE {
    override fun visibleForRoles() = setOf(PROBATION_COM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_ADDRESS_AND_RISK_CHECKS, ADDRESS_AND_RISK_CHECKS_IN_PROGRESS -> LOCKED
      else -> null
    }
  },

  CONFIRM_RELEASE {
    override fun visibleForRoles() = setOf(PRISON_DM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_DECISION, AWAITING_REFUSAL -> READY_TO_START
      TIMED_OUT, OPTED_OUT -> LOCKED
      else -> null
    }
  },

  APPROVE_LICENCE {
    override fun visibleForRoles() = setOf(PRISON_DM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      AWAITING_DECISION, AWAITING_REFUSAL, TIMED_OUT, OPTED_OUT -> LOCKED
      APPROVED -> READY_TO_START
      else -> null
    }
  },

  OPT_IN {
    override fun visibleForRoles() = setOf(PRISON_DM)
    override fun taskStatus(assessment: Assessment): TaskStatus? = when (assessment.status) {
      OPTED_OUT -> LOCKED
      else -> null
    }
  },
  ;

  open fun visibleForRoles() = emptySet<UserRole>()
  abstract fun taskStatus(assessment: Assessment): TaskStatus?

  companion object {
    val TASKS_BY_ROLE = Task.entries
      .flatMap { task -> task.visibleForRoles().map { role -> role to task } }
      .groupBy({ (role) -> role }, { (_, task) -> task })
  }
}

enum class TaskStatus {
  LOCKED,
  READY_TO_START,
  IN_PROGRESS,
  COMPLETE,
}
