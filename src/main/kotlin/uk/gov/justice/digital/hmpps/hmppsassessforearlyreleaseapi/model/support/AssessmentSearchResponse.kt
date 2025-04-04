package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import java.time.LocalDateTime

@Schema(description = "Response object which describes an assessment")
data class AssessmentSearchResponse(

  @Schema(description = "The assessment id", example = "722", required = true)
  val id: Long,

  @Schema(description = "The offender's booking id", example = "773722", required = true)
  val bookingId: Long,

  @Schema(description = "The status", example = "NOT_STARTED", required = true)
  val status: AssessmentStatus,

  @Schema(description = "The previous status", example = "NOT_STARTED", required = false)
  val previousStatus: AssessmentStatus?,

  @Schema(description = "The create timestamp for the assessment", example = "2020-01-11 12:13:00", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val createdTimestamp: LocalDateTime,

  @Schema(description = "The update timestamp for the assessment", example = "2020-01-11 12:13:00", required = false)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val lastUpdatedTimestamp: LocalDateTime?,

  @Schema(description = "The delete timestamp for the assessment", example = "2020-01-11 12:13:00", required = false)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val deletedTimestamp: LocalDateTime?,
)
