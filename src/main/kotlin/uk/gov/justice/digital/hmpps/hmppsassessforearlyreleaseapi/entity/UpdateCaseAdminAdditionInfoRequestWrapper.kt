package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateCaseAdminAdditionInfoRequest

data class UpdateCaseAdminAdditionInfoRequestWrapper(
  val updateCaseAdminAdditionInfoRequest: UpdateCaseAdminAdditionInfoRequest,
  val agent: Agent,
)
