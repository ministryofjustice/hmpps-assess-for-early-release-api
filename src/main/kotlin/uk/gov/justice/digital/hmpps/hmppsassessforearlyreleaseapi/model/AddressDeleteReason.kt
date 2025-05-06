package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.AddressDeleteReasonType
import kotlin.reflect.KClass

@ValidAddressDeleteReason
@Schema(description = "Records an offender's non disclosable information")
data class AddressDeleteReasonDto(
  @Schema(description = "The reason why address deleted", example = "NO_LONGER_WANTS_TO_BE_RELEASED_HERE")
  @field:NotNull
  val addressDeleteReasonType: AddressDeleteReasonType?,

  @Schema(description = "Other reason to delete address", example = "Give details of information regards delete address")
  val addressDeleteOtherReason: String?,
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AddressDeleteReasonValidator::class])
annotation class ValidAddressDeleteReason(
  val message: String = "If addressDeleteReason is OTHER_REASON, addressDeleteOtherReason must not be null or empty",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class AddressDeleteReasonValidator : ConstraintValidator<ValidAddressDeleteReason, AddressDeleteReasonDto> {
  override fun isValid(value: AddressDeleteReasonDto, context: ConstraintValidatorContext?): Boolean = !(value.addressDeleteReasonType == AddressDeleteReasonType.OTHER_REASON && value.addressDeleteOtherReason.isNullOrBlank())
}
