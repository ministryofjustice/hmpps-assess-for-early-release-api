package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderSearchResponse

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByPrisonNumber(prisonerNumber: String): Offender?

  @Query(
    "SELECT new uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support." +
      "OffenderSearchResponse(o.prisonNumber,o.prisonId,o.forename,o.surname,o.dateOfBirth,o.crn) " +
      "FROM Offender o " +
      "WHERE o.prisonNumber LIKE %:searchString% " +
      "   OR o.crn LIKE %:searchString%",
  )
  fun searchForOffender(@Param("searchString") searchString: String): List<OffenderSearchResponse>
}
