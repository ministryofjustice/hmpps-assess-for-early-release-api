package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import com.fasterxml.jackson.annotation.JsonFormat
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest
import java.time.LocalDate

@Entity
@DiscriminatorValue(value = "police-check")
class PoliceChecksTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  criterionMet: Boolean,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  var answers: PoliceChecksAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.POLICE_CHECK.taskCode,
  criterionMet = criterionMet,
  taskVersion = taskVersion,
) {
  override fun toAnswersMap(): Map<String, Any?> = mapOf(
    "informationRequested" to answers.informationRequested,
    "informationSent" to answers.informationSent,
    "informationSummary" to answers.informationSummary,
  )

  override fun getAnswers(): AnswerPayload = answers

  override fun updateAnswers(answers: AnswerPayload): ResidentialChecksTaskAnswer {
    this.answers = answers as PoliceChecksAnswers
    return this
  }

  override fun toString(): String = "PoliceChecksTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "informationRequested=${answers.informationRequested}, " +
    "informationSent=${answers.informationSent}, " +
    "informationSummary=${answers.informationSummary}, " +
    ")"
}

data class PoliceChecksAnswers(
  @JsonFormat(pattern = "yyyy-MM-dd")
  @field:NotNull(message = "Enter a valid date in the past that you requested information")
  @field:Past(message = "Enter a valid date in the past that you requested information")
  val informationRequested: LocalDate?,

  @JsonFormat(pattern = "yyyy-MM-dd")
  @field:NotNull(message = "Enter a valid date in the past that the information was sent")
  @field:Past(message = "Enter a valid date in the past that the information was sent")
  val informationSent: LocalDate?,

  @field:NotBlank(message = "Enter a summary of the information received")
  @field:Size(max = 1000, message = "Enter a maximum of 1000 characters")
  val informationSummary: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    criterionMet: Boolean,
    taskVersion: String,
  ): ResidentialChecksTaskAnswer = PoliceChecksTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    criterionMet = criterionMet,
    taskVersion = taskVersion,
  )
}
