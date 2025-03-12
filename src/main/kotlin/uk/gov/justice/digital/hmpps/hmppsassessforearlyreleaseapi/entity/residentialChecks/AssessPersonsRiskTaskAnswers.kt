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
@DiscriminatorValue(value = "assess-this-persons-risk")
class AssessPersonsRiskTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  criterionMet: Boolean,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  var answers: AssessPersonsRiskAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.ASSESS_THIS_PERSONS_RISK.taskCode,
  criterionMet = criterionMet,
  taskVersion = taskVersion,
) {
  override fun toAnswersMap(): Map<String, Any?> = mapOf(
    "pomPrisonBehaviourInformation" to answers.pomPrisonBehaviourInformation,
    "mentalHealthTreatmentNeeds" to answers.mentalHealthTreatmentNeeds,
    "vloOfficerForCase" to answers.vloOfficerForCase,
    "informationThatCannotBeDisclosed" to answers.informationThatCannotBeDisclosed,
  )

  override fun getAnswers(): AnswerPayload = answers

  override fun updateAnswers(answers: AnswerPayload): ResidentialChecksTaskAnswer {
    this.answers = answers as AssessPersonsRiskAnswers
    return this
  }

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
  @field:NotNull(message = "Enter information provided by the POM about behaviour in prison")
  @field:NotBlank(message = "Enter information provided by the POM about behaviour in prison")
  @field:Size(max = 1000, message = "Please enter a maximum of 1000 characters")
  val pomPrisonBehaviourInformation: String?,

  @field:NotNull(message = "Select if the person has mental health treatment needs")
  val mentalHealthTreatmentNeeds: Boolean?,

  @field:NotNull(message = "Select if there is a VLO officer for the case")
  val vloOfficerForCase: Boolean?,

  @field:NotNull(message = "Select if there is information that cannot be disclosed")
  val informationThatCannotBeDisclosed: Boolean?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    criterionMet: Boolean,
    taskVersion: String,
  ): ResidentialChecksTaskAnswer = AssessPersonsRiskTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    criterionMet = criterionMet,
    taskVersion = taskVersion,
  )
}
