package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
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
  var forename: String,

  @NotNull
  var surname: String,

  var phoneNumber: String?,

  var relation: String?,

  var dateOfBirth: LocalDate?,

  var age: Int?,

  @NotNull
  var isMainResident: Boolean,

  @NotNull
  var isOffender: Boolean,

  @ManyToOne(optional = false)
  @JoinColumn(name = "standard_address_check_request_id", nullable = false)
  var standardAddressCheckRequest: StandardAddressCheckRequest,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
