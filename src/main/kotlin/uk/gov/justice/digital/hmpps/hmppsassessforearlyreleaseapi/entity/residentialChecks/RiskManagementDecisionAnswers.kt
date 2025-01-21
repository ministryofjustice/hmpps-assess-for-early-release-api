package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest

@Entity
@DiscriminatorValue(value = "make-a-risk-management-decision")
class RiskManagementDecisionTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  criterionMet: Boolean,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  var answers: RiskManagementDecisionAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  criterionMet = criterionMet,
  taskCode = ResidentialChecksTaskAnswerType.MAKE_A_RISK_MANAGEMENT_DECISION.taskCode,
  taskVersion = taskVersion,
) {
  override fun toAnswersMap(): Map<String, Any?> = mapOf(
    "canOffenderBeManagedSafely" to answers.canOffenderBeManagedSafely,
    "informationToSupportDecision" to answers.informationToSupportDecision,
    "riskManagementPlanningActionsNeeded" to answers.riskManagementPlanningActionsNeeded,
  )

  override fun getAnswers(): AnswerPayload = answers

  override fun updateAnswers(answers: AnswerPayload): ResidentialChecksTaskAnswer {
    this.answers = answers as RiskManagementDecisionAnswers
    return this
  }

  override fun toString(): String = "RiskManagementDecisionTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "canOffenderBeManagedSafely=${answers.canOffenderBeManagedSafely}, " +
    "informationToSupportDecision=${answers.informationToSupportDecision}, " +
    "riskManagementPlanningActionsNeeded=${answers.riskManagementPlanningActionsNeeded}" +
    ")"
}

data class RiskManagementDecisionAnswers(
  @field:NotNull(message = "Select if the offender can be managed safely in the community")
  val canOffenderBeManagedSafely: Boolean?,

  @field:NotNull(message = "Enter information to support the decision")
  @field:NotBlank(message = "Enter information to support the decision")
  @field:Size(max = 1000, message = "Enter a maximum of 1000 characters")
  val informationToSupportDecision: String?,

  @field:NotNull(message = "Select if any risk management planning actions are needed")
  val riskManagementPlanningActionsNeeded: Boolean?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    criterionMet: Boolean,
    taskVersion: String,
  ): ResidentialChecksTaskAnswer = RiskManagementDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    criterionMet = criterionMet,
    taskVersion = taskVersion,
  )
}
