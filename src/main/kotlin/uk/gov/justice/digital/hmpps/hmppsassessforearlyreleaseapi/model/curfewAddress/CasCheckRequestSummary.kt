package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressPreferencePriority
import java.time.LocalDateTime

@Schema(description = "Response object which describes a CAS check request")
@JsonTypeName(CheckRequestType.CAS)
data class CasCheckRequestSummary(
  @Schema(description = "Type of this check request", example = CheckRequestType.CAS, allowableValues = [CheckRequestType.CAS])
  override val requestType: String = CheckRequestType.CAS,

  @Schema(description = "Unique internal identifier for this request", example = "123344")
  override val requestId: Long,

  @Schema(description = "Any additional information on the request added by the case administrator", example = "Some additional info")
  override val caAdditionalInfo: String?,

  @Schema(description = "Any additional information on the request added by the probation practitioner", example = "Some additional info")
  override val ppAdditionalInfo: String?,

  @Schema(description = "The date / time the check was requested on", example = "22/11/2026 10:43:28")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  override val dateRequested: LocalDateTime,

  @Schema(description = "The priority of the check request", example = "SECOND")
  override val preferencePriority: AddressPreferencePriority,

  @Schema(description = "The status of the check request", example = "SUITABLE")
  override val status: AddressCheckRequestStatus,

  @Schema(description = "The address the check request is for", example = "See AddressSummary")
  val allocatedAddress: AddressSummary?,
) : CheckRequestSummary
