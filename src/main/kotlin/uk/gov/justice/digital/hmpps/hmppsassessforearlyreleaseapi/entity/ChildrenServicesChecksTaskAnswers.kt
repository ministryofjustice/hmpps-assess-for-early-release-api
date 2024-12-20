package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.ResidentialChecksTaskAnswerType.CHILDREN_SERVICES_CHECK
import java.time.LocalDate

@Entity
@DiscriminatorValue(value = "CHILDREN_SERVICES_CHECK")
class ChildrenServicesChecksTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskCode: String,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: ChildrenServicesChecksAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = taskCode,
  taskVersion = taskVersion,
  answerType = CHILDREN_SERVICES_CHECK,
) {

  override fun toString(): String = "ChildrenServicesTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "informationRequested=${answers.informationRequested}, " +
    "informationSent=${answers.informationSent}, " +
    "informationSummary=${answers.informationSummary}, " +
    ")"
}

data class ChildrenServicesChecksAnswers(
  @field:Past
  val informationRequested: LocalDate,

  @field:Past
  val informationSent: LocalDate,

  @NotNull
  @NotBlank
  @Size(min = 1, max = 1000)
  val informationSummary: String,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = ChildrenServicesChecksTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "children-services-check",
    taskVersion = taskVersion,
  )
}
