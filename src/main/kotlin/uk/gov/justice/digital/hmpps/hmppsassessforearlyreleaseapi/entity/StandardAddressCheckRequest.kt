package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class StandardAddressCheckRequest(
  id: Long = -1,
  caAdditionalInfo: String? = null,
  ppAdditionalInfo: String? = null,
  dateRequested: LocalDateTime = LocalDateTime.now(),
  preferencePriority: AddressPreferencePriority,
  status: AddressCheckRequestStatus = AddressCheckRequestStatus.IN_PROGRESS,
  assessment: Assessment,
  @ManyToOne
  @JoinColumn(name = "address_id", referencedColumnName = "id")
  val address: Address,
  @OneToMany(mappedBy = "standardAddressCheckRequest", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val residents: MutableSet<Resident> = mutableSetOf(),
) : CurfewAddressCheckRequest(
  id = id,
  caAdditionalInfo = caAdditionalInfo,
  ppAdditionalInfo = ppAdditionalInfo,
  dateRequested = dateRequested,
  preferencePriority = preferencePriority,
  status = status,
  assessment = assessment,
)
