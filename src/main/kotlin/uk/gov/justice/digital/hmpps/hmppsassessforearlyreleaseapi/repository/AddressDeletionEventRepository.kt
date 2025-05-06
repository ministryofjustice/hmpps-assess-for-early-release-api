package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressDeletionEvent

interface AddressDeletionEventRepository : JpaRepository<AddressDeletionEvent, Long>
