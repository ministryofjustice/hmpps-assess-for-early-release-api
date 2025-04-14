package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import java.time.LocalDate
import kotlin.reflect.KClass

@ValidRelation
@Schema(description = "Request for adding a resident to a standard address check request")
data class AddResidentRequest(
  @Schema(description = "A unique internal reference for the resident", example = "87320")
  val residentId: Long? = null,

  @Schema(description = "The resident's forename", example = "Dave")
  val forename: String,

  @Schema(description = "The resident's surname", example = "Jones")
  val surname: String,

  @Schema(description = "The resident's phone number", example = "07634183674")
  val phoneNumber: String? = null,

  @Schema(description = "The resident's relation to the offender", example = "Mother")
  val relation: String? = null,

  @Schema(description = "The resident's date of birth", example = "2002-02-20")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The resident's age", example = "42")
  val age: Int? = null,

  @Schema(description = "Is this main resident at the address", example = "true")
  val isMainResident: Boolean,

  @Schema(description = "Is offender a main resident at the address", example = "true")
  val isOffender: Boolean,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RelationValidator::class])
annotation class ValidRelation(
  val message: String = "Relation must be provided if the resident is not an offender",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class RelationValidator : ConstraintValidator<ValidRelation, AddResidentRequest> {
  override fun isValid(request: AddResidentRequest, context: ConstraintValidatorContext): Boolean = request.isOffender || !request.relation.isNullOrBlank()
}
