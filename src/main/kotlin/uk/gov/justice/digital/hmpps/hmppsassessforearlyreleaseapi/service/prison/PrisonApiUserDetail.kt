package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class PrisonApiUserDetail(
  val staffId: Long,
  val username: String,
  val firstName: String,
  val lastName: String,
  val thumbnailId: Long? = null,
  val activeCaseLoadId: String? = null,
  val accountStatus: String,
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val lockDate: LocalDateTime? = null,
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val expiryDate: LocalDateTime? = null,
  val lockedFlag: Boolean? = null,
  val expiredFlag: Boolean? = null,
  val active: Boolean,
)
