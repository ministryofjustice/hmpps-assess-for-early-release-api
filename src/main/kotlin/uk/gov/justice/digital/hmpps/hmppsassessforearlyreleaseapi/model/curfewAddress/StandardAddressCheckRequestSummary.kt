package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressDeleteReasonDto
import java.time.LocalDateTime

@Schema(description = "Response object which describes a standard address check request")
@JsonTypeName(CheckRequestType.STANDARD_ADDRESS)
data class StandardAddressCheckRequestSummary(
  @Schema(description = "Type of this check request", example = CheckRequestType.STANDARD_ADDRESS, allowableValues = [CheckRequestType.STANDARD_ADDRESS])
  override val requestType: String = CheckRequestType.STANDARD_ADDRESS,

  @Schema(description = "Unique internal identifier for this request", example = "123344")
  override val requestId: Long,

  @Schema(description = "Any additional information on the request added by the case administrator", example = "Some additional info")
  override val caAdditionalInfo: String? = null,

  @Schema(description = "Any additional information on the request added by the probation practitioner", example = "Some additional info")
  override val ppAdditionalInfo: String? = null,

  @Schema(description = "The date / time the check was requested on", example = "22/11/2026 10:43:28")
  override val dateRequested: LocalDateTime,

  @Schema(description = "The priority of the check request", example = "SECOND")
  override val preferencePriority: AddressPreferencePriority,

  @Schema(description = "The status of the check request", example = "SUITABLE")
  override val status: AddressCheckRequestStatus,

  @Schema(description = "The address the check request is for", example = "See AddressSummary")
  val address: AddressSummary,

  @Schema(description = "The residents the check request is for", example = "See ResidentSummary")
  val residents: List<ResidentSummary>,

  @Schema(description = "The reason for deleting address", example = "See reason type and text")
  val deleteReason: AddressDeleteReasonDto? = null,
) : CheckRequestSummary
