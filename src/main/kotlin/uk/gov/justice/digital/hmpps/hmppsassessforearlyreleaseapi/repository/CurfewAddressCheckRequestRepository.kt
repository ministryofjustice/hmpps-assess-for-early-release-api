package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest

interface CurfewAddressCheckRequestRepository : JpaRepository<CurfewAddressCheckRequest, Long> {
  fun findByAssessment(assessment: Assessment): List<CurfewAddressCheckRequest>
}
