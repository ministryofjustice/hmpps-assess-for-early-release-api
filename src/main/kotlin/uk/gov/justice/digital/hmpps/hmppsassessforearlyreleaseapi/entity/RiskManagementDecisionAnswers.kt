package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.ResidentialChecksTaskAnswerType.MAKE_A_RISK_MANAGEMENT_DECISION

@Entity
@DiscriminatorValue(value = "MAKE_A_RISK_MANAGEMENT_DECISION")
class RiskManagementDecisionTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskCode: String,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: RiskManagementDecisionAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = taskCode,
  taskVersion = taskVersion,
  answerType = MAKE_A_RISK_MANAGEMENT_DECISION,
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
  val canOffenderBeManagedSafely: Boolean,
  val informationToSupportDecision: String,
  val riskManagementPlanningActionsNeeded: Boolean,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = RiskManagementDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "make-a-risk-management-decision",
    taskVersion = taskVersion,
  )
}
