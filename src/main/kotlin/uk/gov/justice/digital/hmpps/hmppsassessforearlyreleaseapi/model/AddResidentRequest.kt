package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

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
  val relation: String,

  @Schema(description = "The resident's date of birth", example = "2002-02-20")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The resident's age", example = "42")
  val age: Int? = null,

  @Schema(description = "Is this main resident at the address", example = "true")
  val isMainResident: Boolean,
)
