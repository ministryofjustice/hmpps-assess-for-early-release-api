package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswer
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class CurfewAddressCheckRequest(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  var caAdditionalInfo: String? = null,

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

  @OneToMany(mappedBy = "addressCheckRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
  val taskAnswers: MutableSet<ResidentialChecksTaskAnswer> = mutableSetOf(),

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
