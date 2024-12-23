package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Response object which describes an offender")
data class OffenderSummary(
  @Schema(description = "The offender's prisoner number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The offender's booking id", example = "773722")
  val bookingId: Long,

  @Schema(description = "The offender's first name", example = "Bob")
  val forename: String?,

  @Schema(description = "The offender's surname", example = "Smith")
  val surname: String?,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "2026-08-23")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val hdced: LocalDate,

  @Schema(description = "The full name of the probation practitioner responsible for this offender", example = "Mark Coombes")
  val probationPractitioner: String? = null,
)
