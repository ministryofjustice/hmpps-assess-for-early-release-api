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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest
import java.time.LocalDate

@Entity
@DiscriminatorValue(value = "children-services-check")
class ChildrenServicesChecksTaskAnswers(
  id: Long = -1L,
  addressCheckRequest: CurfewAddressCheckRequest,
  taskVersion: String,
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val answers: ChildrenServicesChecksAnswers,
) : ResidentialChecksTaskAnswer(
  id = id,
  addressCheckRequest = addressCheckRequest,
  taskCode = ResidentialChecksTaskAnswerType.CHILDREN_SERVICES_CHECK.taskCode,
  taskVersion = taskVersion,
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
  @JsonFormat(pattern = "yyyy-MM-dd")
  @field:Past
  val informationRequested: LocalDate?,

  @JsonFormat(pattern = "yyyy-MM-dd")
  @field:Past
  val informationSent: LocalDate?,

  @NotNull
  @NotBlank
  @Size(min = 1, max = 1000)
  val informationSummary: String?,
) : AnswerPayload {
  override fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer = ChildrenServicesChecksTaskAnswers(
    answers = this,
    addressCheckRequest = addressCheckRequest,
    taskVersion = taskVersion,
  )
}
