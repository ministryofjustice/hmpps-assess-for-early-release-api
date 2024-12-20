package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.ResidentialChecksTaskAnswerType.SUITABILITY_DECISION

@Entity
@DiscriminatorValue(value = "SUITABILITY_DECISION")
class SuitabilityDecisionTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskCode: String,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: SuitabilityDecisionAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = taskCode,
  taskVersion = taskVersion,
  answerType = SUITABILITY_DECISION,
) {
  override fun toString(): String = "SuitabilityDecision(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "addressSuitable=${answers.addressSuitable}, " +
    "addressSuitableInformation=${answers.addressSuitableInformation}, " +
    "additionalInformationNeeded=${answers.additionalInformationNeeded}, " +
    "moreInformation=${answers.moreInformation}, " +
    ")"
}

data class SuitabilityDecisionAnswers(
  @NotNull
  val addressSuitable: Boolean?,

  @NotNull
  @NotBlank
  @Size(min = 1, max = 1000)
  val addressSuitableInformation: String?,

  @NotNull
  val additionalInformationNeeded: Boolean,

  @NotNull
  @NotBlank
  @Size(min = 1, max = 1000)
  val moreInformation: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = SuitabilityDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "suitability-decision",
    taskVersion = taskVersion,
  )
}
