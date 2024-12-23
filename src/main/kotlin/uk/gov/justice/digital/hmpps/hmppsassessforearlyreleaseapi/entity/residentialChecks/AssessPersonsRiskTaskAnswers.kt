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
@DiscriminatorValue(value = "assess-this-persons-risk")
class AssessPersonsRiskTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: AssessPersonsRiskAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.ASSESS_THIS_PERSONS_RISK.taskCode,
  taskVersion = taskVersion,
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
  @field:NotNull
  @field:NotBlank
  @field:Size(min = 1, max = 1000)
  val pomPrisonBehaviourInformation: String?,

  @field:NotNull
  val mentalHealthTreatmentNeeds: Boolean?,

  @field:NotNull
  val vloOfficerForCase: Boolean?,

  @field:NotNull
  val informationThatCannotBeDisclosed: Boolean?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = AssessPersonsRiskTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskVersion = taskVersion,
  )
}
