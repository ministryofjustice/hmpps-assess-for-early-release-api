package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class BankHolidayResult(
  val events: List<BankHolidayEvent>,
)

data class BankHoliday(
  @JsonProperty("england-and-wales")
  val bankHolidayResult: BankHolidayResult,
)

data class BankHolidayEvent(
  @JsonFormat(pattern = "yyyy-MM-dd")
  val date: LocalDate,
)
