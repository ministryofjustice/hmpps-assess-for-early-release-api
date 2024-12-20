package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.ResidentialChecksTaskAnswerType.POLICE_CHECK
import java.time.LocalDate

@Entity
@DiscriminatorValue(value = "POLICE_CHECK")
class PoliceChecksTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskCode: String,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: PoliceChecksAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = taskCode,
  taskVersion = taskVersion,
  answerType = POLICE_CHECK,
) {
  override fun toString(): String = "PoliceChecksTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "informationRequested=${answers.informationRequested}, " +
    "informationSent=${answers.informationSent}, " +
    "informationSummary=${answers.informationSummary}, " +
    ")"
}

data class PoliceChecksAnswers(
  @field:Past
  val informationRequested: LocalDate,

  @field:Past
  val informationSent: LocalDate,

  @field:NotBlank
  @field:Size(min = 1, max = 1000)
  val informationSummary: String,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = PoliceChecksTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "police-check",
    taskVersion = taskVersion,
  )
}
