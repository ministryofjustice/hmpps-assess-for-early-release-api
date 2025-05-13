package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.Address
import java.time.LocalDateTime

@Entity
@Table(name = "cas_accommodation_assessment", schema = "public")
class CasAccommodationAssessment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  val id: Long = -1,
  @ManyToOne
  @JoinColumn(name = "assessment_id", referencedColumnName = "id", nullable = false)
  val assessment: Assessment,
  @Enumerated(EnumType.STRING)
  private var status: CasStatus,
  @Enumerated(EnumType.STRING)
  var type: CasType? = null,
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "address_id", nullable = false)
  var address: Address? = null,
  @Column
  var ineligibilityReason: String? = null,
  @Column
  var areasToAvoidInfo: String? = null,
  @Column
  var supportingInfoForReferral: String? = null,
  @Column
  var referred: Boolean? = null,
  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),
  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as CasAccommodationAssessment
    return this.id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  fun setStatus(status: CasStatus) {
    this.status = this.status.getValidTo(status)
  }
  fun getStatus() = this.status
}
