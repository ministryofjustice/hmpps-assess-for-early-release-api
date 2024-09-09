package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.task

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CacheEvictTask {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @Scheduled(fixedRateString = "PT12H")
  @CacheEvict(value = ["prisons"], allEntries = true)
  fun evictPrisonRegisterCache() {
    log.debug("Evicting prison register cache.")
  }
}
