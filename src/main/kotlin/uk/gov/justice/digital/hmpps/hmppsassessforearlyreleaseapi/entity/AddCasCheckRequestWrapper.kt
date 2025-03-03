package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest

data class AddCasCheckRequestWrapper(
  @Valid val addCasCheckRequest: AddCasCheckRequest,
  @Valid val agent: Agent,
)
