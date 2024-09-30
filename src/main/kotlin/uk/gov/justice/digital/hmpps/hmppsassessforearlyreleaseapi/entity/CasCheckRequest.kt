package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class CasCheckRequest(
  id: Long = -1,
  caAdditionalInfo: String?,
  ppAdditionalInfo: String?,
  dateRequested: LocalDate,
  preferencePriority: AddressPreferencePriority,
  status: AddressCheckRequestStatus,
  @ManyToOne
  @JoinColumn(name = "allocated_address_id", referencedColumnName = "id")
  val allocatedAddress: Address,
) : CurfewAddressCheckRequest(
  id = id,
  caAdditionalInfo = caAdditionalInfo,
  ppAdditionalInfo = ppAdditionalInfo,
  dateRequested = dateRequested,
  preferencePriority = preferencePriority,
  status = status,
)
