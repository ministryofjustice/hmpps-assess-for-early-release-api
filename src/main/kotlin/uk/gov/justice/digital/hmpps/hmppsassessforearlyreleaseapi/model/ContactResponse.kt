package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole

@Schema(description = "Details of a contact")
data class ContactResponse(

  @Schema(description = "The full name of the contact", example = "Bob Smith")
  val fullName: String,

  @Schema(description = "The contact type", example = "CASE_ADMINISTRATOR")
  val userRole: UserRole,

  @Schema(description = "The email address of the contact", example = "bob.jones@justice.gov.uk")
  val email: String?,

  @Schema(description = "The location name of the contact", example = "Foston Hall (HMP)")
  val locationName: String? = null,
)
