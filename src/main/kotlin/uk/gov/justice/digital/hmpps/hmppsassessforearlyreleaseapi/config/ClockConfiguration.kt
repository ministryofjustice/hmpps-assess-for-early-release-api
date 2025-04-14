package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val API_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
const val API_DATE_FORMAT = "yyyy-MM-dd"

@Configuration
class ClockConfiguration {
  @Bean
  fun clock(): Clock = Clock.systemDefaultZone()

  companion object {
    val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(API_DATE_TIME_FORMAT)
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(API_DATE_FORMAT)
  }

  @Bean
  fun javaTimeModule(): Module {
    val module = JavaTimeModule()
    module.addSerializer(LocalDateTimeSerializer(DATE_TIME_FORMATTER))
    module.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(DATE_TIME_FORMATTER))
    module.addSerializer(LocalDateSerializer(DATE_FORMATTER))
    module.addDeserializer(LocalDate::class.java, LocalDateDeserializer(DATE_FORMATTER))
    return module
  }
}
