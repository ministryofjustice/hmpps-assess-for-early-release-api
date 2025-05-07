package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus

sealed class AssessmentLifecycleEvent {
  data class EligibilityAnswerProvided(
    val type: CriteriaType,
    val code: String,
    val answers: Map<String, Boolean>,
  ) : AssessmentLifecycleEvent() {
    override fun getContext(): Map<String, Any> = mapOf("type" to type, "code" to code, "answers" to answers)
  }

  data class EligibilityChecksPassed(
    val type: CriteriaType,
    val code: String,
    val answers: Map<String, Boolean>,
  ) : AssessmentLifecycleEvent() {
    override fun getContext(): Map<String, Any> = mapOf("type" to type, "code" to code, "answers" to answers)
  }

  data class EligibilityChecksFailed(
    val type: CriteriaType,
    val code: String,
    val answers: Map<String, Boolean>,
  ) : AssessmentLifecycleEvent() {
    override fun getContext(): Map<String, Any> = mapOf("type" to type, "code" to code, "answers" to answers)
  }

  data object SubmitForAddressChecks : AssessmentLifecycleEvent()

  data object StartAddressChecks : AssessmentLifecycleEvent()

  data class ResidentialCheckAnswerProvided(
    val checkStatus: ResidentialChecksStatus,
    val taskCode: String,
    val answers: Map<String, Any>,
  ) : AssessmentLifecycleEvent() {
    override fun getContext(): Map<String, Any> = mapOf("checkStatus" to checkStatus, "taskCode" to taskCode, "answers" to answers)
  }

  data object CompleteAddressChecks : AssessmentLifecycleEvent()

  data object FailAddressChecks : AssessmentLifecycleEvent()

  data object SubmitForDecision : AssessmentLifecycleEvent()

  data object Approve : AssessmentLifecycleEvent()

  data object Refuse : AssessmentLifecycleEvent()

  data class OptOut(val reason: OptOutReasonType, val otherDescription: String?) : AssessmentLifecycleEvent() {
    override fun getContext(): Map<String, Any> = mapOf("reason" to reason as Any, "otherDescription" to (otherDescription ?: "description not provided") as Any)
  }

  data object OptBackIn : AssessmentLifecycleEvent()

  data object Timeout : AssessmentLifecycleEvent()

  data class Postpone(val reasonTypes: LinkedHashSet<PostponeCaseReasonType>) : AssessmentLifecycleEvent() {
    override fun getContext(): Map<String, Any> = mapOf("reasonTypes" to reasonTypes)
  }

  data object ReleaseOnHDC : AssessmentLifecycleEvent()

  open fun getContext(): Map<String, Any> = emptyMap()
}
