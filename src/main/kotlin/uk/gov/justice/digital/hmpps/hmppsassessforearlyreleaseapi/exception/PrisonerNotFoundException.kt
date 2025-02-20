package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception

class PrisonerNotFoundException(val prisonCode: String) : RuntimeException()
