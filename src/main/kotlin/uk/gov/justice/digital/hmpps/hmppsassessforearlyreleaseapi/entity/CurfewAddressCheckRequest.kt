package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class CurfewAddressCheckRequest(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  val caAdditionalInfo: String? = null,

  val ppAdditionalInfo: String? = null,

  @NotNull
  val dateRequested: LocalDateTime = LocalDateTime.now(),

  @NotNull
  @Enumerated(EnumType.STRING)
  val preferencePriority: AddressPreferencePriority,

  @NotNull
  @Enumerated(EnumType.STRING)
  val status: AddressCheckRequestStatus,

  @ManyToOne
  @JoinColumn(name = "assessment_id", referencedColumnName = "id", nullable = false)
  val assessment: Assessment,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
