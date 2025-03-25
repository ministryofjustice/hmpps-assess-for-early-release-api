package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

class WorkingDaysServiceTest {
  private val bankHolidayService = mock<BankHolidayService>()
  private val clock = Clock.fixed(Instant.parse("2025-02-10T00:00:00Z"), ZoneId.systemDefault())
  private val today = LocalDate.now(clock)
  private val service = WorkingDaysService(bankHolidayService, clock)

  @BeforeEach
  fun reset() {
    org.mockito.kotlin.reset(bankHolidayService)
    whenever(bankHolidayService.getBankHolidaysForEnglandAndWales()).thenReturn(someBankHolidays)
  }

  @Nested
  inner class Weekends {
    @Test
    fun `is date on the weekend`() {
      val today = LocalDate.of(2024, 3, 23)
      val isWeekend = service.isWeekend(today)
      assertThat(isWeekend).isTrue()
    }

    @Test
    fun `is date not on the weekend`() {
      val today = LocalDate.of(2024, 3, 21)
      val isWeekend = service.isWeekend(today)
      assertThat(isWeekend).isFalse()
    }
  }

  @Nested
  inner class `Is working day or not` {
    @Test
    fun `is date a non working day`() {
      val today = LocalDate.of(2024, 3, 23)
      val isNonWorkingDay = service.isNonWorkingDay(today)
      assertThat(isNonWorkingDay).isTrue()
    }

    @Test
    fun `is date a working day`() {
      val today = LocalDate.of(2024, 3, 21)
      val isNonWorkingDay = service.isNonWorkingDay(today)
      assertThat(isNonWorkingDay).isFalse()
    }

    @Test
    fun `is bank holiday a non working day`() {
      val today = LocalDate.of(2024, 12, 25)
      val isBankHoliday = service.isNonWorkingDay(today)
      assertThat(isBankHoliday).isTrue()
    }
  }

  @Nested
  inner class `Working days until a date` {
    @Test
    fun `get working days until a week from now`() {
      val nextWeek = today.plusDays(7)
      val workingDaysBefore = service.workingDaysUntil(nextWeek)
      assertThat(workingDaysBefore).isEqualTo(5)
    }

    @Test
    fun `No working days before today`() {
      val workingDaysBefore = service.workingDaysUntil(today)
      assertThat(workingDaysBefore).isZero()
    }

    @Test
    fun `bank holidays do not count as working days`() {
      org.mockito.kotlin.reset(bankHolidayService)
      val bankHolidays = listOf(today.plusDays(1), today.plusDays(2))
      whenever(bankHolidayService.getBankHolidaysForEnglandAndWales()).thenReturn(bankHolidays)

      val workingDaysBefore = service.workingDaysUntil(today.plusDays(7))
      assertThat(workingDaysBefore).isEqualTo(3)
    }
  }

  @Nested
  inner class `Working days before a date` {
    @Test
    fun `get previous working day on a weekday`() {
      val today = LocalDate.of(2024, Month.MARCH, 21)
      val previousWorkingDay = service.workingDaysBefore(today).take(1).first()
      assertThat(LocalDate.of(2024, Month.MARCH, 20)).isEqualTo(previousWorkingDay)
    }

    @Test
    fun `get previous working day on a Monday`() {
      val today = LocalDate.of(2024, Month.MARCH, 25)
      val previousWorkingDay = service.workingDaysBefore(today).take(1).first()
      assertThat(LocalDate.of(2024, Month.MARCH, 22)).isEqualTo(previousWorkingDay)
    }

    @Test
    fun `get previous working day on a bank holiday`() {
      val today = LocalDate.of(2024, Month.MAY, 7)
      val previousWorkingDay = service.workingDaysBefore(today).take(1).first()
      assertThat(LocalDate.of(2024, Month.MAY, 3)).isEqualTo(previousWorkingDay)
    }

    @Test
    fun `get previous working day on a multi day bank holiday`() {
      val today = LocalDate.of(2024, 4, 2)
      val previousWorkingDay = service.workingDaysBefore(today).take(1).first()
      assertThat(LocalDate.of(2024, 3, 28)).isEqualTo(previousWorkingDay)
    }

    @Test
    fun `check sequence is made up of only working days`() {
      val today = LocalDate.of(2024, 3, 21)
      val previousWorkingDays = service.workingDaysBefore(today).take(3)
      previousWorkingDays.forEach {
        assertThat(service.isNonWorkingDay(it)).isFalse()
      }
      assertThat(previousWorkingDays.toList()).isEqualTo(
        listOf(
          LocalDate.of(2024, 3, 20),
          LocalDate.of(2024, 3, 19),
          LocalDate.of(2024, 3, 18),
        ),
      )
    }
  }

  @Nested
  inner class `Working days after a date` {
    @Test
    fun `get next working day on a weekday`() {
      val today = LocalDate.of(2024, Month.MARCH, 21)
      val nextWorkingDay = service.workingDaysAfter(today).take(1).first()
      assertThat(LocalDate.of(2024, Month.MARCH, 22)).isEqualTo(nextWorkingDay)
    }

    @Test
    fun `get next working day on a Friday with a bank holiday on Monday`() {
      val today = LocalDate.of(2024, Month.MARCH, 29)
      val nextWorkingDay = service.workingDaysAfter(today).take(1).first()
      assertThat(LocalDate.of(2024, Month.APRIL, 2)).isEqualTo(nextWorkingDay)
    }

    @Test
    fun `check sequence is made up of only working days`() {
      val today = LocalDate.of(2024, 3, 21)
      val nextWorkingDays = service.workingDaysAfter(today).take(3)
      nextWorkingDays.forEach {
        assertThat(service.isNonWorkingDay(it)).isFalse()
      }
    }
  }

  private companion object {
    val someBankHolidays = listOf(
      LocalDate.parse("2024-03-29"),
      LocalDate.parse("2024-04-01"),
      LocalDate.parse("2024-05-06"),
      LocalDate.parse("2024-08-26"),
      LocalDate.parse("2024-12-25"),
      LocalDate.parse("2024-12-26"),
      LocalDate.parse("2025-01-01"),
    )
  }
}
