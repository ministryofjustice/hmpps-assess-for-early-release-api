package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.client.mangeUsers

import org.springframework.stereotype.Service

@Service
class ManagedUsersService(
  private val managedUsersApiClient: ManagedUsersApiClient,
) {

  fun getEmail(username: String): ManagedUserEmailResponse? = managedUsersApiClient.getEmail(username)
}
