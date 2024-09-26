package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Response object which describes an address")
data class AddressSummary(
  @Schema(description = "The address's UPRN", example = "200010019924")
  val uprn: String,

  @Schema(description = "The address's first line", example = "34 Maryport Street")
  val firstLine: String? = null,

  @Schema(description = "The address's second line", example = "Urchfont")
  val secondLine: String? = null,

  @Schema(description = "The address's town", example = "Chippenham")
  val town: String,

  @Schema(description = "The address's county", example = "Shropshire")
  val county: String,

  @Schema(description = "The address's postcode", example = "RG13HS")
  val postcode: String,

  @Schema(description = "The address's country", example = "Wales")
  val country: String,

  @Schema(description = "The address's x-coordinate", example = "401003.0,")
  val xCoordinate: Double,

  @Schema(description = "The address's y-coordinate", example = "154111.0")
  val yCoordinate: Double,

  @Schema(description = "The date the address was last updated", example = "2021-05-23")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val addressLastUpdated: LocalDate,
)
