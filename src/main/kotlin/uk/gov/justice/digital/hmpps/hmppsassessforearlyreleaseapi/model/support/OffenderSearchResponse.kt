package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import java.time.LocalDate

@Schema(description = "Response object which describes an offender")
data class OffenderSearchResponse(
  @Schema(description = "The offender's prisoner number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The prison code", example = "BRS")
  val prisonId: String,

  @Schema(description = "The offender's first name", example = "Kalitta")
  val forename: String,

  @Schema(description = "The offender's surname", example = "Amexar")
  val surname: String,

  @Schema(description = "The offender's date of birth", example = "2002-02-20")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val dateOfBirth: LocalDate,

  @Schema(description = "The case reference number assigned to a person on probation in NDelius ", example = "DX12340A", required = false)
  val crn: String? = null,
)
