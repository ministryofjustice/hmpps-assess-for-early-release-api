package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "Request object for updating the COM responsible for an offender")
data class UpdateCom(

  @Schema(description = "The unique code of the COM, retrieved from Delius", example = "22003829")
  @field:NotNull
  val staffCode: String,

  @Schema(description = "The Delius username for the COM", example = "jbloggs")
  @field:NotNull
  val staffUsername: String,

  @Schema(description = "The email address of the COM", example = "jbloggs@probation.gov.uk")
  val staffEmail: String?,

  @Schema(description = "The first name of the COM", example = "Joseph")
  val forename: String,

  @Schema(description = "The last name of the COM", example = "Bloggs")
  val surname: String,

  @Schema(description = "The team the COM is assigned to", example = "N55LAU")
  val team: String,
)
