package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "offender")
data class Offender(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long,

  @NotNull
  val bookingId: Long,

  @NotNull
  val prisonerNumber: String,

  val prisonId: String,

  val firstName: String? = null,

  val lastName: String? = null,

  @NotNull
  val hdced: LocalDate,

  @NotNull
  @Enumerated(EnumType.STRING)
  val status: OffenderStatus = OffenderStatus.NOT_STARTED,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
