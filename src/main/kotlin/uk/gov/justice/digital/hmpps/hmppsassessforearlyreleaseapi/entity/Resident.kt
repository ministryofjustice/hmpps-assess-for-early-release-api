package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import software.amazon.awssdk.annotations.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "resident")
data class Resident(
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = -1,

  @NotNull
  val forename: String,

  @NotNull
  val surname: String,

  val phoneNumber: String?,

  @NotNull
  val relation: String,

  val dateOfBirth: LocalDate?,

  val age: Int?,

  @NotNull
  val isMainResident: Boolean,

  @ManyToOne(optional = false)
  val standardAddressCheckRequest: StandardAddressCheckRequest,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
