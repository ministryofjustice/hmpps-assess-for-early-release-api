package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception

import org.springframework.validation.Errors

class TaskAnswersValidationException(val taskCode: String, val error: Errors) : Exception()
