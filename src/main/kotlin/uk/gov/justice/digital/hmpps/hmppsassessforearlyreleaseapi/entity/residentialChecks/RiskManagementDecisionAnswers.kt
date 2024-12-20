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
  @NotNull
  val canOffenderBeManagedSafely: Boolean?,

  @NotNull
  @NotBlank
  @Size(min = 1, max = 1000)
  val informationToSupportDecision: String?,

  @NotNull
  val riskManagementPlanningActionsNeeded: Boolean?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = RiskManagementDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskVersion = taskVersion,
  )
}
