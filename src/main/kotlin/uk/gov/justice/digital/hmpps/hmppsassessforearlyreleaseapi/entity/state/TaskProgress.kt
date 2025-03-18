package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus

sealed interface TaskProgress {
  val task: Task
  val status: (assessment: Assessment) -> TaskStatus

  class Fixed(override val task: Task, status: TaskStatus) : TaskProgress {
    override val status: (assessment: Assessment) -> TaskStatus = { status }
  }

  class Dynamic(override val task: Task, check: (assessment: Assessment) -> TaskStatus) : TaskProgress {
    override val status: (assessment: Assessment) -> TaskStatus = check
  }
}
