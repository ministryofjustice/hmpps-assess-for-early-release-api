package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import java.time.LocalDateTime

@Schema(description = "Response object which describes a CAS¡ check request")
data class CasCheckRequestSummary(
  @Schema(description = "Any additional information on the request added by the case administrator", example = "Some additional info")
  val caAdditionalInfo: String?,

  @Schema(description = "Any additional information on the request added by the probation practitioner", example = "Some additional info")
  val ppAdditionalInfo: String?,

  @Schema(description = "The date / time the check was requested on", example = "22/11/2026 10:43:28")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val dateRequested: LocalDateTime,

  @Schema(description = "The priority of the check request", example = "SECOND")
  val preferencePriority: AddressPreferencePriority,

  @Schema(description = "The status of the check request", example = "SUITABLE")
  val status: AddressCheckRequestStatus,

  @Schema(description = "The address the check request is for", example = "See AddressSummary")
  val allocatedAddress: AddressSummary?,
)
