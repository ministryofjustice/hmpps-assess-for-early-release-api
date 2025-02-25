package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import software.amazon.awssdk.annotations.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Entity
@Table(name = "resident")
@ValidRelation
data class Resident(
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = -1,

  @NotNull
  var forename: String,

  @NotNull
  var surname: String,

  var phoneNumber: String?,

  var relation: String?,

  var dateOfBirth: LocalDate?,

  var age: Int?,

  @NotNull
  var isMainResident: Boolean,

  @NotNull
  var isOffender: Boolean,

  @ManyToOne(optional = false)
  @JoinColumn(name = "standard_address_check_request_id", nullable = false)
  @JsonIgnore
  var standardAddressCheckRequest: StandardAddressCheckRequest,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RelationValidator::class])
annotation class ValidRelation(
  val message: String = "Relation must be provided if the resident is not an offender",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class RelationValidator : ConstraintValidator<ValidRelation, Resident> {
  override fun isValid(resident: Resident, context: ConstraintValidatorContext): Boolean = resident.isOffender || !resident.relation.isNullOrBlank()
}
