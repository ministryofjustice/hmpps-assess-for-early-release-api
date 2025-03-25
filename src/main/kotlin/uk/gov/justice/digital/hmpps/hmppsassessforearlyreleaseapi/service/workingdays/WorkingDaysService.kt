package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate

@Service
class WorkingDaysService(private val bankHolidayService: BankHolidayService, private val clock: Clock) {
  fun workingDaysUntil(
    date: LocalDate,
  ): Int = generateSequence(date) {
    if (it > LocalDate.now(clock)) {
      it.minusDays(1)
    } else {
      null
    }
  }
    .drop(1)
    .filterNot { isNonWorkingDay(it) }
    .count()

  fun workingDaysBefore(
    date: LocalDate,
  ): Sequence<LocalDate> = generateSequence(date) { it.minusDays(1) }
    .drop(1)
    .filterNot { isNonWorkingDay(it) }

  fun workingDaysAfter(
    date: LocalDate,
  ): Sequence<LocalDate> = generateSequence(date) { it.plusDays(1) }
    .drop(1)
    .filterNot { isNonWorkingDay(it) }

  fun isWeekend(date: LocalDate): Boolean = date.dayOfWeek in weekend

  fun isNonWorkingDay(date: LocalDate): Boolean = isWeekend(date) || getBankHolidays().contains(date)

  private fun getBankHolidays(): List<LocalDate> = bankHolidayService.getBankHolidaysForEnglandAndWales()

  companion object {
    val weekend = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
  }
}
