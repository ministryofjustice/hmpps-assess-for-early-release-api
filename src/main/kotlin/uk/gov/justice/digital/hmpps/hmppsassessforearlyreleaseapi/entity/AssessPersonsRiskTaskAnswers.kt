package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.ResidentialChecksTaskAnswerType.ASSESS_THIS_PERSONS_RISK

@Entity
@DiscriminatorValue(value = "ASSESS_THIS_PERSONS_RISK")
class AssessPersonsRiskTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskCode: String,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: AssessPersonsRiskAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = taskCode,
  taskVersion = taskVersion,
  answerType = ASSESS_THIS_PERSONS_RISK,
) {
  override fun toString(): String = "AssessPersonsRiskTaskAnswers(" +
    "id=$id, " +
    "addressCheckRequest=${addressCheckRequest.id}, " +
    "pomPrisonBehaviourInformation=${answers.pomPrisonBehaviourInformation}, " +
    "mentalHealthTreatmentNeeds=${answers.mentalHealthTreatmentNeeds}, " +
    "vloOfficerForCase=${answers.vloOfficerForCase}, " +
    "informationThatCannotBeDisclosed=${answers.informationThatCannotBeDisclosed}, " +
    ")"
}

data class AssessPersonsRiskAnswers(
  val pomPrisonBehaviourInformation: String,
  val mentalHealthTreatmentNeeds: Boolean,
  val vloOfficerForCase: Boolean,
  val informationThatCannotBeDisclosed: Boolean,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = AssessPersonsRiskTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskCode = "ASSESS_THIS_PERSONS_RISK",
    taskVersion = taskVersion,
  )
}
