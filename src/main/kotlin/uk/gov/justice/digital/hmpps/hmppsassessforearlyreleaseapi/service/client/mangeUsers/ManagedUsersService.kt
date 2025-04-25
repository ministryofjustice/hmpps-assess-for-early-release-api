package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.client.mangeUsers

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException

@Service
class ManagedUsersService(
  private val managedUsersApiClient: ManagedUsersApiClient,
) {
  fun getEmail(username: String): ManagedUserEmailResponse {
    val managedUserEmailResponse = managedUsersApiClient.getEmail(username)
    return managedUserEmailResponse ?: throw ItemNotFoundException("Cannot find email for username $username")
  }
}
