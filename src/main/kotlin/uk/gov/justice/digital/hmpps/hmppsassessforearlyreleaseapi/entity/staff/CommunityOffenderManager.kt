package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.staff

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Creator
import java.time.LocalDateTime

@Entity
@DiscriminatorValue(value = "COMMUNITY_OFFENDER_MANAGER")
class CommunityOffenderManager(
  id: Long = -1,
  val staffCode: String,
  username: String?,
  email: String?,
  forename: String?,
  surname: String?,
  lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) : Staff(
  id = id,
  kind = StaffKind.COMMUNITY_OFFENDER_MANAGER,
  username = username,
  email = email,
  forename = forename,
  surname = surname,
  lastUpdatedTimestamp = lastUpdatedTimestamp,
),
  Creator {
  fun copy(
    id: Long = this.id,
    staffCode: String = this.staffCode,
    username: String? = this.username,
    email: String? = this.email,
    forename: String? = this.forename,
    surname: String? = this.surname,
    lastUpdatedTimestamp: LocalDateTime = this.lastUpdatedTimestamp,
  ) = CommunityOffenderManager(
    id = id,
    staffCode = staffCode,
    username = username,
    email = email,
    forename = forename,
    surname = surname,
    lastUpdatedTimestamp = lastUpdatedTimestamp,
  )
}
