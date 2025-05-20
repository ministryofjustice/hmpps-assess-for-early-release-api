package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import java.time.LocalDate

@Schema(description = "Response object which describes an offender")
data class OffenderSummaryResponse(
  @Schema(description = "The offender's prisoner number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The offender's booking id", example = "773722")
  val bookingId: Long,

  @Schema(description = "The offender's first name", example = "Kalitta")
  val forename: String,

  @Schema(description = "The offender's surname", example = "Amexar")
  val surname: String,

  @Schema(description = "The offender's conditional release date date", example = "2026-08-23")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val crd: LocalDate? = null,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "2026-08-23")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val hdced: LocalDate,

  @Schema(description = "The number of working days until the home detention curfew eligibility date", example = "15")
  val workingDaysToHdced: Int,

  @Schema(description = "The full name of the probation practitioner responsible for this offender", example = "Mark Coombes", required = false)
  val probationPractitioner: String? = null,

  @Schema(description = "Whether the offender's current assessment has been postponed or not", example = "True")
  val isPostponed: Boolean = false,

  @Schema(description = "The date that the offender's current assessment was postponed", example = "2028-06-23", required = false)
  @JsonFormat(pattern = API_DATE_FORMAT)
  val postponementDate: LocalDate? = null,

  @Schema(description = "The reasons that the offender's current assessment was postponed", example = "ON_REMAND")
  val postponementReasons: List<PostponeCaseReasonType> = listOf(),

  @Schema(description = "The status of the offender's current assessment", example = "AWAITING_ADDRESS_AND_RISK_CHECKS")
  val status: AssessmentStatus,

  @Schema(description = "Whether the address checks for the offender's current assessment have been completed or not", example = "false")
  val addressChecksComplete: Boolean = false,

  @Schema(description = "The current task for the offender's current assessment, if there is no next task then null will be returned", example = "Assess eligibility and suitability")
  val currentTask: Task?,

  @Schema(description = "The date that the current task overdue on", example = "2028-06-23", required = false)
  val taskOverdueOn: LocalDate? = null,

  @Schema(description = "The case reference number assigned to a person on probation in NDelius ", example = "DX12340A", required = false)
  val crn: String? = null,

  @Schema(description = "Last updated by", example = "Bura Hurn")
  val lastUpdateBy: String? = null,
)
