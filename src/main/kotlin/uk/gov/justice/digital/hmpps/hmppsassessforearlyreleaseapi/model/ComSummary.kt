package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.staff.CommunityOffenderManager

@Schema(description = "A summary of a community offender manager")
data class ComSummary(
  @Schema(description = "The staff code", example = "A01B02C")
  val staffCode: String,

  @Schema(description = "The username", example = "X33221")
  val username: String?,

  @Schema(description = "The offender managers email address", example = "Kalitta.Amexar@justice.gov.uk")
  val email: String?,

  @Schema(description = "The offender managers first name", example = "Kalitta")
  val forename: String?,

  @Schema(description = "The offender managers surname", example = "Amexar")
  val surname: String?,
)

fun CommunityOffenderManager.toSummary(): ComSummary = ComSummary(
  staffCode = this.staffCode,
  username = this.username,
  email = this.email,
  forename = this.forename,
  surname = this.surname,
)
