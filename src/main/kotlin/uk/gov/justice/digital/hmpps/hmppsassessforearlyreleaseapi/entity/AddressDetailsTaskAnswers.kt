package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.ResidentialChecksTaskAnswerType.ADDRESS_DETAILS_AND_INFORMED_CONSENT

@Entity
@DiscriminatorValue(value = "ADDRESS_DETAILS_AND_INFORMED_CONSENT")
class AddressDetailsTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskCode: String,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: AddressDetailsAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = taskCode,
  taskVersion = taskVersion,
  answerType = ADDRESS_DETAILS_AND_INFORMED_CONSENT,
) {
  override fun toString(): String = "AddressDetailsTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "electricitySupply=${answers.electricitySupply}, " +
    "visitedAddress=${answers.visitedAddress}, " +
    "mainOccupierConsentGiven=${answers.mainOccupierConsentGiven}, " +
    ")"
}

data class AddressDetailsAnswers(
  @field:NotNull
  val electricitySupply: Boolean?,

  @field:NotNull
  val visitedAddress: Boolean?,

  @field:NotNull
  val mainOccupierConsentGiven: Boolean?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = AddressDetailsTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "address-details-and-informed-consent",
    taskVersion = taskVersion,
  )
}
