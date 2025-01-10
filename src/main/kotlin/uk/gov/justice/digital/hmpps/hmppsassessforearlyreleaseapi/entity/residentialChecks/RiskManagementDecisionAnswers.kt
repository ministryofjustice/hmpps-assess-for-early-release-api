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
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: RiskManagementDecisionAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.MAKE_A_RISK_MANAGEMENT_DECISION.taskCode,
  taskVersion = taskVersion,
) {
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
  val canOffenderBeManagedSafely: String?,

  @field:NotNull(message = "Enter information to support the decision")
  @field:NotBlank(message = "Enter information to support the decision")
  @field:Size(max = 1000, message = "Enter a maximum of 1000 characters")
  val informationToSupportDecision: String?,

  @field:NotNull(message = "Select if any risk management planning actions are needed")
  val riskManagementPlanningActionsNeeded: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = RiskManagementDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskVersion = taskVersion,
  )
}
