package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class HmppsAssessForEarlyReleaseApi

fun main(args: Array<String>) {
  runApplication<HmppsAssessForEarlyReleaseApi>(*args)
}
