package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.PdfConfigProperties

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(PdfConfigProperties::class)
class HmppsAssessForEarlyReleaseApi

fun main(args: Array<String>) {
  runApplication<HmppsAssessForEarlyReleaseApi>(*args)
}
