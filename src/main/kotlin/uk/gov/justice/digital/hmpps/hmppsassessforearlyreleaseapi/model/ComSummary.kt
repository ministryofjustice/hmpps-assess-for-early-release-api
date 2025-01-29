package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager

@Schema(description = "A summary of a community offender manager")
data class ComSummary(
  @Schema(description = "The staff code", example = "A01B02C")
  val staffCode: String,

  @Schema(description = "The username", example = "X33221")
  val username: String?,

  @Schema(description = "The offender managers email address", example = "bob.jones@justice.gov.uk")
  val email: String?,

  @Schema(description = "The offender managers first name", example = "Bob")
  val forename: String?,

  @Schema(description = "The offender managers surname", example = "Jones")
  val surname: String?,

  @Schema(description = "The team the offender manager is assigned to", example = "N55LAU")
  val team: String?,
)

fun CommunityOffenderManager.toSummary(): ComSummary = ComSummary(
  staffCode = this.staffCode,
  username = this.username,
  email = this.email,
  forename = this.forename,
  surname = this.surname,
  team = this.team,
)
