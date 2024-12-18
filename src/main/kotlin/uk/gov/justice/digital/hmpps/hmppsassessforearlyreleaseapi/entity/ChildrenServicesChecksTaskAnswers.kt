package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
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
    "dateRequested=${answers.dateRequested}, " +
    "dateSent=${answers.dateSent}, " +
    "informationSummary=${answers.informationSummary}, " +
    ")"
}

data class ChildrenServicesChecksAnswers(
  val dateRequested: LocalDate,
  val dateSent: LocalDate,
  val informationSummary: String,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = ChildrenServicesChecksTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "CHILDREN_SERVICES_CHECK",
    taskVersion = taskVersion,
  )
}
