package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception

import org.springframework.validation.Errors

class TaskAnswersValidationException(val taskCode: String, val errors: Errors) : Exception()
