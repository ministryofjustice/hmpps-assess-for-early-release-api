package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import java.time.LocalDate

@Schema(description = "Response object which describes an address")
data class AddressSummary(
  @Schema(description = "The address's UPRN", example = "200010019924")
  val uprn: String,

  @Schema(description = "The address's first line", example = "1 Test Street")
  val firstLine: String? = null,

  @Schema(description = "The address's second line", example = "Off Test Road")
  val secondLine: String? = null,

  @Schema(description = "The address's town", example = "Test Town")
  val town: String,

  @Schema(description = "The address's county", example = "Test County")
  val county: String,

  @Schema(description = "The address's postcode", example = "Test Postcode")
  val postcode: String,

  @Schema(description = "The address's country", example = "Test Country")
  val country: String,

  @Schema(description = "The address's x-coordinate", example = "401003.0,")
  val xcoordinate: Double,

  @Schema(description = "The address's y-coordinate", example = "154111.0")
  val ycoordinate: Double,

  @Schema(description = "The date the address was last updated", example = "2021-05-23")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val addressLastUpdated: LocalDate,

)
