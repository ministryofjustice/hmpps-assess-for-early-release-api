package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderResponse

@Component
class OffenderToOffenderResponseMapper {

  fun map(offender: Offender): OffenderResponse = OffenderResponse(
    prisonNumber = offender.prisonNumber,
    forename = offender.forename!!,
    surname = offender.surname!!,
    crd = offender.crd,
    crn = offender.crn,
    prisonId = offender.prisonId,
    dateOfBirth = offender.dateOfBirth,
    sentenceStartDate = offender.sentenceStartDate,
    createdTimestamp = offender.createdTimestamp,
    lastUpdatedTimestamp = offender.lastUpdatedTimestamp,
  )
}
