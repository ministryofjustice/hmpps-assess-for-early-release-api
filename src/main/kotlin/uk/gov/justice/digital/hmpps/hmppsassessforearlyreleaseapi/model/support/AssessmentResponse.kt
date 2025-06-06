package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_TIME_FORMAT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ComSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Response object which describes an assessment")
data class AssessmentResponse(

  @Schema(description = "The assessment id", example = "722", required = true)
  val id: Long,

  @Schema(description = "The offender's booking id", example = "773722", required = true)
  val bookingId: Long,

  @Schema(description = "The status", example = "NOT_STARTED", required = true)
  val status: AssessmentStatus,

  @Schema(description = "The previous status", example = "NOT_STARTED", required = false)
  val previousStatus: AssessmentStatus?,

  @Schema(description = "The create timestamp for the assessment", example = "2020-01-11T12:13:00", required = true)
  @JsonFormat(pattern = API_DATE_TIME_FORMAT)
  val createdTimestamp: LocalDateTime,

  @Schema(description = "The update timestamp for the assessment", example = "2020-01-11T12:13:00", required = false)
  @JsonFormat(pattern = API_DATE_TIME_FORMAT)
  val lastUpdatedTimestamp: LocalDateTime?,

  @Schema(description = "The delete timestamp for the assessment", example = "2020-01-11T12:13:00", required = false)
  @JsonFormat(pattern = API_DATE_TIME_FORMAT)
  val deletedTimestamp: LocalDateTime?,

  @Schema(description = "The version of the policy that this assessment has been carried out under", example = "1.0", required = true)
  val policyVersion: String,

  @Schema(description = "Assessment address checks complete", required = true)
  val addressChecksComplete: Boolean = false,

  @Schema(description = "The community offender manager assigned to this assessment", required = false)
  val responsibleCom: ComSummary?,

  @Schema(description = "The team that the COM responsible for this assessment is assigned to", example = "Team1", required = false)
  val teamCode: String?,

  @Schema(description = "The postponement date", example = "2026-08-23", required = false)
  @JsonFormat(pattern = API_DATE_FORMAT)
  val postponementDate: LocalDate?,

  @Schema(description = "The opt out reason type", required = false)
  val optOutReasonType: OptOutReasonType?,

  @Schema(description = "The opt out reason description if rhe optOutReasonType is OTHER", required = true)
  val optOutReasonOther: String?,

  @Schema(description = "The home detention curfew eligibility date", example = "2026-08-23", required = true)
  @JsonFormat(pattern = API_DATE_FORMAT)
  val hdced: LocalDate,

  @Schema(description = "The offender's conditional release date date", example = "2026-08-23", required = false)
  @JsonFormat(pattern = API_DATE_FORMAT)
  val crd: LocalDate? = null,

  @Schema(description = "The sentence start date", example = "2028-06-23", required = false)
  @JsonFormat(pattern = API_DATE_FORMAT)
  val sentenceStartDate: LocalDate? = null,
)
