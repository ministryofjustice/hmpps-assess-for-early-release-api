package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
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

  @Schema(description = "The number of working days until the home detention curfew eligibility date", example = "15")
  val workingDaysToHdced: Int,

  @Schema(description = "The full name of the probation practitioner responsible for this offender", example = "Mark Coombes")
  val probationPractitioner: String? = null,

  @Schema(description = "Whether the offender's current assessment has been postponed or not", example = "True")
  val isPostponed: Boolean = false,

  @Schema(description = "The date that the offender's current assessment was postponed", example = "2028-06-23")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val postponementDate: LocalDate? = null,

  @Schema(description = "The reason that the offender's current assessment was postponed", example = "Have an application pending with the unduly lenient sentence scheme")
  val postponementReason: String? = null,

  @Schema(description = "The status of the offender's current assessment", example = "AWAITING_ADDRESS_AND_RISK_CHECKS")
  val status: AssessmentStatus,

  @Schema(description = "Whether the address checks for the offender's current assessment have been completed or not", example = "false")
  val addressChecksComplete: Boolean = false,

  @Schema(description = "The date that the current task overdue on", example = "false")
  val taskOverdueOn: LocalDate? = null,
)
