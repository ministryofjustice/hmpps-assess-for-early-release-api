package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "address")
data class Address(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = -1,

  val uprn: String? = null,

  val firstLine: String? = null,

  val secondLine: String? = null,

  @NotNull
  val town: String,

  @NotNull
  val county: String? = null,

  @NotNull
  val postcode: String,

  @NotNull
  val country: String? = null,

  @NotNull
  val xCoordinate: Double? = null,

  @NotNull
  val yCoordinate: Double? = null,

  @NotNull
  val addressLastUpdated: LocalDate,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
