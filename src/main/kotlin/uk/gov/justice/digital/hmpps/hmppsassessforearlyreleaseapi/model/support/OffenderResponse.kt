package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
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
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "2026-08-23")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val hdced: LocalDate,

  @Schema(description = "The offender's conditional release date date", example = "2026-08-23")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val crd: LocalDate? = null,

  @Schema(description = "The case reference number assigned to a person on probation in NDelius ", example = "DX12340A", required = false)
  val crn: String? = null,

  @Schema(description = "The sentence start date", example = "2028-06-23", required = false)
  val sentenceStartDate: LocalDate? = null,

  @Schema(description = "The create timestamp for the afer offender", example = "2020-01-11 12:13:00", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val createdTimestamp: LocalDateTime? = null,

  @Schema(description = "The offender's conditional release date date", example = "2020-01-11 12:13:00", required = false)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val lastUpdatedTimestamp: LocalDateTime? = null,
)
