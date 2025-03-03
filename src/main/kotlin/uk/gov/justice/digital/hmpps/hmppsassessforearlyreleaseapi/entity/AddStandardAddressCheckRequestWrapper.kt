package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest

data class AddStandardAddressCheckRequestWrapper(
  @Valid val addStandardAddressCheckRequest: AddStandardAddressCheckRequest,
  @Valid val agent: Agent,
)
