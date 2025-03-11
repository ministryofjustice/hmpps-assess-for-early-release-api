package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest

@Entity
@DiscriminatorValue(value = "suitability-decision")
class SuitabilityDecisionTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  criterionMet: Boolean,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  var answers: SuitabilityDecisionAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  criterionMet = criterionMet,
  taskCode = ResidentialChecksTaskAnswerType.SUITABILITY_DECISION.taskCode,
  taskVersion = taskVersion,
) {
  override fun toAnswersMap(): Map<String, Any?> = mapOf(
    "addressSuitable" to answers.addressSuitable,
    "addressSuitableInformation" to answers.addressSuitableInformation,
    "additionalInformationNeeded" to answers.additionalInformationNeeded,
    "moreInformation" to answers.moreInformation,
  )

  override fun getAnswers(): AnswerPayload = answers

  override fun updateAnswers(answers: AnswerPayload): ResidentialChecksTaskAnswer {
    this.answers = answers as SuitabilityDecisionAnswers
    return this
  }

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
  @field:NotNull(message = "Select if the address is suitable for the offender to be released")
  val addressSuitable: Boolean?,

  @field:NotNull(message = "Enter information to support the decision")
  @field:NotBlank(message = "Enter information to support the decision")
  @field:Size(max = 1000, message = "Enter a maximum of 1000 characters")
  val addressSuitableInformation: String?,

  @field:NotNull(message = "Select if you need to add more information")
  val additionalInformationNeeded: Boolean?,

  @field:Size(max = 1000, message = "Enter a maximum of 1000 characters")
  val moreInformation: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    criterionMet: Boolean,
    taskVersion: String,
  ): ResidentialChecksTaskAnswer = SuitabilityDecisionTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    criterionMet = criterionMet,
    taskVersion = taskVersion,
  )
}
