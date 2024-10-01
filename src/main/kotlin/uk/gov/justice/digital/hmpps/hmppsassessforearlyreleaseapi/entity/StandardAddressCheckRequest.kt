package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class StandardAddressCheckRequest(
  id: Long = -1,
  caAdditionalInfo: String?,
  ppAdditionalInfo: String?,
  dateRequested: LocalDateTime = LocalDateTime.now(),
  preferencePriority: AddressPreferencePriority,
  status: AddressCheckRequestStatus = AddressCheckRequestStatus.IN_PROGRESS,
  assessment: Assessment,
  @ManyToOne
  @JoinColumn(name = "address_id", referencedColumnName = "id")
  val address: Address,
) : CurfewAddressCheckRequest(
  id = id,
  caAdditionalInfo = caAdditionalInfo,
  ppAdditionalInfo = ppAdditionalInfo,
  dateRequested = dateRequested,
  preferencePriority = preferencePriority,
  status = status,
  assessment = assessment,
)
