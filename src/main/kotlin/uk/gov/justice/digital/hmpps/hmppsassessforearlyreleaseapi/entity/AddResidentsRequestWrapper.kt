package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddResidentRequest

data class AddResidentsRequestWrapper(
  val addResidentsRequest: List<AddResidentRequest>,
  val agent: Agent,
)
