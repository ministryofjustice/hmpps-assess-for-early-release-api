package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue(value = "COMMUNITY_OFFENDER_MANAGER")
class CommunityOffenderManager(
  id: Long = -1,
  val staffIdentifier: Long,
  username: String?,
  email: String?,
  forename: String,
  surname: String,
  lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) : Staff(
  id = id,
  kind = StaffKind.COMMUNITY_OFFENDER_MANAGER,
  username = username,
  email = email,
  forename = forename,
  surname = surname,
  lastUpdatedTimestamp = lastUpdatedTimestamp,
) {
  @Override
  override fun toString(): String = this::class.simpleName + "(id: $id, username: $username)"
}
