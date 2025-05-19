package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import java.time.LocalDateTime

@Schema(description = "Response object which describes a assessment event")
interface AssessmentEventResponse {
  @get:Schema(description = "type that describes the event")
  val eventType: AssessmentEventType

  @get:Schema(description = "time and date of the event")
  val eventTime: LocalDateTime

  @get:Schema(description = "subject of the event")
  val summary: String

  @get:Schema(description = "username of the person who triggered the event")
  val username: String

  @get:Schema(description = "full name of the person who triggered the event")
  val fullName: String

  @get:Schema(description = "role of the used to trigger the event")
  val role: UserRole

  @get:Schema(description = "Event on Behalf of")
  val onBehalfOf: String?

  @get:Schema(description = "changes that occurred during the event")
  val changes: String?
}
