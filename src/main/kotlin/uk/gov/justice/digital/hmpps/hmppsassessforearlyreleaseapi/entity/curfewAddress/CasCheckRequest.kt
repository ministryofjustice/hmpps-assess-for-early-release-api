package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import java.time.LocalDateTime

@Entity
class CasCheckRequest(
  id: Long = -1,
  caAdditionalInfo: String? = null,
  ppAdditionalInfo: String? = null,
  dateRequested: LocalDateTime = LocalDateTime.now(),
  preferencePriority: AddressPreferencePriority,
  status: AddressCheckRequestStatus = AddressCheckRequestStatus.IN_PROGRESS,
  assessment: Assessment,
  @ManyToOne
  @JoinColumn(name = "allocated_address_id", referencedColumnName = "id")
  val allocatedAddress: Address? = null,
) : CurfewAddressCheckRequest(
  id = id,
  caAdditionalInfo = caAdditionalInfo,
  ppAdditionalInfo = ppAdditionalInfo,
  dateRequested = dateRequested,
  preferencePriority = preferencePriority,
  status = status,
  assessment = assessment,
)
