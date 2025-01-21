package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.VisitedAddress

@Entity
@DiscriminatorValue(value = "address-details-and-informed-consent")
class AddressDetailsTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  criterionMet: Boolean,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  var answers: AddressDetailsAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.ADDRESS_DETAILS_AND_INFORMED_CONSENT.taskCode,
  criterionMet = criterionMet,
  taskVersion = taskVersion,
) {
  override fun toAnswersMap(): Map<String, Any?> = mapOf(
    "electricitySupply" to answers.electricitySupply,
    "visitedAddress" to answers.visitedAddress,
    "mainOccupierConsentGiven" to answers.mainOccupierConsentGiven,
  )

  override fun getAnswers(): AnswerPayload = answers

  override fun updateAnswers(answers: AnswerPayload): ResidentialChecksTaskAnswer {
    this.answers = answers as AddressDetailsAnswers
    return this
  }

  override fun toString(): String = "AddressDetailsTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "electricitySupply=${answers.electricitySupply}, " +
    "visitedAddress=${answers.visitedAddress}, " +
    "mainOccupierConsentGiven=${answers.mainOccupierConsentGiven}, " +
    ")"
}

data class AddressDetailsAnswers(
  @field:NotNull(message = "Select if the address is connected to an electricity supply")
  val electricitySupply: Boolean?,

  @field:NotNull(message = "Select if you have visited the address")
  val visitedAddress: VisitedAddress?,

  @field:NotNull(message = "Select if the main occupier has given consent for the offender to be released")
  val mainOccupierConsentGiven: Boolean?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    criterionMet: Boolean,
    taskVersion: String,
  ): ResidentialChecksTaskAnswer = AddressDetailsTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    criterionMet = criterionMet,
    taskVersion = taskVersion,
  )
}
