package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest

@Entity
@DiscriminatorValue(value = "address-details-and-informed-consent")
class AddressDetailsTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: AddressDetailsAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.ADDRESS_DETAILS_AND_INFORMED_CONSENT.taskCode,
  taskVersion = taskVersion,
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
  @field:NotNull(message = "Select if the address is connected to an electricity supply")
  val electricitySupply: String?,

  @field:NotNull(message = "Select if you have visited the address")
  val visitedAddress: String?,

  @field:NotNull(message = "Select if the main occupier has given consent for the offender to be released")
  val mainOccupierConsentGiven: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = AddressDetailsTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskVersion = taskVersion,
  )
}
