package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.TaskAnswersValidationException

@RestControllerAdvice
class HmppsAssessForEarlyReleaseApiExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(e: ValidationException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("No resource found exception: {}", e.message) }

  @ExceptionHandler(ItemNotFoundException::class)
  fun handlePrisonerNotFoundException(e: ItemNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Item not found exception : ${e.message}",
        developerMessage = "ItemNotFoundException : ${e.message}",
      ),
    ).also {
      log.info(e.message, e)
    }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<uk.gov.justice.hmpps.kotlin.common.ErrorResponse> = ResponseEntity
    .status(HttpStatus.FORBIDDEN)
    .body(
      uk.gov.justice.hmpps.kotlin.common.ErrorResponse(
        status = HttpStatus.FORBIDDEN,
        userMessage = "Forbidden: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.debug("Forbidden (403) returned: {}", e.message) }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Unexpected error: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handleNotFoundException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Could not find entity: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.warn("Could not find entity", e) }

  @ExceptionHandler(TaskAnswersValidationException::class)
  fun handleTaskAnswersValidationException(exception: TaskAnswersValidationException): ProblemDetail {
    val problemDetail = ProblemDetail.forStatus(BAD_REQUEST)
    problemDetail.title = "Invalid task answers"
    problemDetail.detail = exception.message
    problemDetail.setProperty("taskCode", exception.taskCode)
    for (error in exception.errors.fieldErrors) {
      problemDetail.setProperty(error.field, error.defaultMessage)
    }
    return problemDetail
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
