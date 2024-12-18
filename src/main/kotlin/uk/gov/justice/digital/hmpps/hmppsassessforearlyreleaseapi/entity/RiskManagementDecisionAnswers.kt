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
  override fun toString(): String = "SuitabilityDecision(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "addressSuitable=${answers.addressSuitable}, " +
    "addressSuitableInformation=${answers.addressSuitableInformation}, " +
    "additionInformationNeeded=${answers.additionInformationNeeded}, " +
    "moreInformation=${answers.moreInformation}, " +
    ")"
}

data class RiskManagementDecisionAnswers(
  val addressSuitable: Boolean,
  val addressSuitableInformation: String,
  val additionInformationNeeded: Boolean,
  val moreInformation: String,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = RiskManagementDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "MAKE_A_RISK_MANAGEMENT_DECISION",
    taskVersion = taskVersion,
  )
}
