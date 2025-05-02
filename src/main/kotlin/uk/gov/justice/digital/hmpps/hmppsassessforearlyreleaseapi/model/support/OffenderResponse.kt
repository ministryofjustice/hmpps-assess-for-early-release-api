package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_TIME_FORMAT
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Response object which describes an offender")
data class OffenderResponse(
  @Schema(description = "The offender's prisoner number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The prison code", example = "BRS")
  val prisonId: String,

  @Schema(description = "The offender's first name", example = "Bob")
  val forename: String,

  @Schema(description = "The offender's surname", example = "Smith")
  val surname: String,

  @Schema(description = "The offender's date of birth", example = "2002-02-20")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val dateOfBirth: LocalDate,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "2026-08-23")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val hdced: LocalDate,

  @Schema(description = "The offender's conditional release date date", example = "2026-08-23")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val crd: LocalDate? = null,

  @Schema(description = "The case reference number assigned to a person on probation in NDelius ", example = "DX12340A", required = false)
  val crn: String? = null,

  @Schema(description = "The sentence start date", example = "2028-06-23", required = false)
  val sentenceStartDate: LocalDate? = null,

  @Schema(description = "The create timestamp for the afer offender", example = "2020-01-11T12:13:00", required = true)
  @JsonFormat(pattern = API_DATE_TIME_FORMAT)
  val createdTimestamp: LocalDateTime? = null,

  @Schema(description = "The offender's conditional release date date", example = "2020-01-11T12:13:00", required = false)
  @JsonFormat(pattern = API_DATE_TIME_FORMAT)
  val lastUpdatedTimestamp: LocalDateTime? = null,
)
