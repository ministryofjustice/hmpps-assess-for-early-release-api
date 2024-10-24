package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import java.time.LocalDateTime

@Schema(description = "Describes a check request")
object CheckRequestType {
  const val STANDARD_ADDRESS = "STANDARD_ADDRESS"
  const val CAS = "CAS"
}

@Schema(
  description = "Describes a check request, a discriminator exists to distinguish between different types of check requests",
  oneOf = [StandardAddressCheckRequestSummary::class, CasCheckRequestSummary::class],
  discriminatorProperty = "requestType",
  discriminatorMapping = [
    DiscriminatorMapping(value = CheckRequestType.STANDARD_ADDRESS, schema = StandardAddressCheckRequestSummary::class),
    DiscriminatorMapping(value = CheckRequestType.CAS, schema = CasCheckRequestSummary::class),
  ],
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "requestType")
sealed interface CheckRequestSummary {
  val requestType: String

  @get:Schema(description = "Unique internal identifier for this request", example = "123344")
  val requestId: Long

  @get:Schema(
    description = "Any additional information on the request added by the case administrator",
    example = "Some additional info",
  )
  val caAdditionalInfo: String?

  @get:Schema(
    description = "Any additional information on the request added by the probation practitioner",
    example = "Some additional info",
  )
  val ppAdditionalInfo: String?

  @get:Schema(description = "The date / time the check was requested on", example = "22/11/2026 10:43:28")
  @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val dateRequested: LocalDateTime

  @get:Schema(description = "The priority of the check request", example = "SECOND")
  val preferencePriority: AddressPreferencePriority

  @get:Schema(description = "The status of the check request", example = "SUITABLE")
  val status: AddressCheckRequestStatus
}
