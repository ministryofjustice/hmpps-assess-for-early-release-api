package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PrisonerSearchPrisoner(
  val prisonerNumber: String,

  val bookingId: Long? = null,

  @JsonFormat(pattern = "yyyy-MM-dd")
  val homeDetentionCurfewEligibilityDate: LocalDate? = null,

  @JsonFormat(pattern = "yyyy-MM-dd")
  val conditionalReleaseDate: LocalDate? = null,

  @JsonFormat(pattern = "yyyy-MM-dd")
  val sentenceStartDate: LocalDate? = null,

  val prisonId: String? = null,

  val firstName: String,

  val lastName: String,

  val dateOfBirth: LocalDate,

  val cellLocation: String? = null,

  val mostSeriousOffence: String? = null,

  val prisonName: String,
)
