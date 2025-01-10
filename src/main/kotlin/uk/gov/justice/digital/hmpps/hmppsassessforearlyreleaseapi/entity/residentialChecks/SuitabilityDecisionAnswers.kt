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
@DiscriminatorValue(value = "suitability-decision")
class SuitabilityDecisionTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: SuitabilityDecisionAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.SUITABILITY_DECISION.taskCode,
  taskVersion = taskVersion,
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
  @NotNull(message = "Select if the address is suitable for the offender to be released")
  val addressSuitable: String?,

  @NotNull(message = "Enter information to support the decision")
  @NotBlank(message = "Enter information to support the decision")
  @Size(min = 1, max = 1000, message = "Enter a maximum of 1000 characters")
  val addressSuitableInformation: String?,

  @NotNull(message = "Select if you need to add more information")
  val additionalInformationNeeded: String,

  val moreInformation: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = SuitabilityDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskVersion = taskVersion,
  )
}
