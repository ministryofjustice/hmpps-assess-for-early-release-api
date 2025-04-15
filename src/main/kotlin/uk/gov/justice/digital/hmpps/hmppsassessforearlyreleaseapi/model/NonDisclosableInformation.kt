package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import jakarta.validation.constraints.NotNull
import kotlin.reflect.KClass

@ValidNonDisclosableInformation
@Schema(description = "Records an offender's non disclosable information")
data class NonDisclosableInformation(
  @Schema(description = "Is there any non disclosable information", example = "true")
  @field:NotNull
  val hasNonDisclosableInformation: Boolean = false,

  @Schema(description = "Information that must not be disclosed to offender", example = "Give details of information that cannot be disclosed.")
  val nonDisclosableInformation: String?,
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NonDisclosableInformationValidator::class])
annotation class ValidNonDisclosableInformation(
  val message: String = "If hasNonDisclosableInformation is true, nonDisclosableInformation must not be null or empty",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class NonDisclosableInformationValidator : ConstraintValidator<ValidNonDisclosableInformation, NonDisclosableInformation> {
  override fun isValid(value: NonDisclosableInformation, context: ConstraintValidatorContext?): Boolean = !value.hasNonDisclosableInformation || !value.nonDisclosableInformation.isNullOrBlank()
}
