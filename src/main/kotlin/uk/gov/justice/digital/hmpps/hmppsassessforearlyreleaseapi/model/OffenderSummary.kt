package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Response object which describes an offender")
data class OffenderSummary(
  @Schema(description = "The offender's prisoner number", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "The bookingId associated with the licence", example = "773722")
  val bookingId: Long,

  @Schema(description = "The offender's first name", example = "Bob")
  val firstName: String?,

  @Schema(description = "The offender's surname", example = "Bob")
  val lastName: String?,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "22/11/2026")
  @JsonFormat(pattern = "dd/MM/yyyy")
  val hdced: LocalDate,
)
