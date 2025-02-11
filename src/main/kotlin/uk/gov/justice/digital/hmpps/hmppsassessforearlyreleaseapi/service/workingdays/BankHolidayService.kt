package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class BankHolidayService(
  private val govUkApiClient: GovUkApiClient,
) {

  @Cacheable("bank-holidays")
  fun getBankHolidaysForEnglandAndWales() = govUkApiClient.getBankHolidaysForEnglandAndWales()
}
