package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.AddPrisonerEligibilityInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.Cas2ReferralInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentAddressRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentOutcomeRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentTypeRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationStatusInfoResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.FlagCasAccommodationAssessmentForReferralRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHolder
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.CasAccommodationAssessmentService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class CasAccommodationAssessmentResource(
  private val casAccommodationAssessmentService: CasAccommodationAssessmentService,
  private val agentHolder: AgentHolder,
) {

  @PostMapping("/offender/{prisonNumber}/current-assessment/cas/requested")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Given prisoner has requested a CAS accommodation.",
    description = "Given prisoner has requested a CAS accommodation.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun prisonerRequestsCasAssessment(
    @Parameter(required = true) @PathVariable prisonNumber: String,
  ) = casAccommodationAssessmentService.prisonerRequestsCasAssessment(prisonNumber, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/cas/assessment/{reference}/eligibility")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Adds prisoner eligibility Info to CAS accommodation assessment.",
    description = "Add prisoner eligibility Info to CAS accommodation assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addPrisonerEligibilityInfo(
    @Parameter(required = true) @PathVariable reference: Long,
    @RequestBody @Valid addPrisonerEligibilityInfoRequest: AddPrisonerEligibilityInfoRequest,
  ) = casAccommodationAssessmentService.addPrisonerEligibilityInfo(reference, addPrisonerEligibilityInfoRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/cas/assessment/{reference}/type")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Set CAS type for accommodation assessment.",
    description = "Set CAS type for accommodation assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun setCasType(
    @Parameter(required = true) @PathVariable reference: Long,
    @RequestBody @Valid casAccommodationAssessmentTypeRequest: CasAccommodationAssessmentTypeRequest,
  ) = casAccommodationAssessmentService.setCasType(reference, casAccommodationAssessmentTypeRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/cas/assessment/{reference}/is-referred")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Flags CAS accommodation assessment has been referred.",
    description = "Flags CAS accommodation assessment has been referred",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun flagForReferral(
    @Parameter(required = true) @PathVariable reference: Long,
    @RequestBody @Valid flagCasAccommodationAssessmentForReferralRequest: FlagCasAccommodationAssessmentForReferralRequest,
  ) = casAccommodationAssessmentService.flagForReferral(reference, flagCasAccommodationAssessmentForReferralRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/cas/assessment/{reference}/cas-2/add-referral-info")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Adds CAS2 referral info to the CAS accommodation assessment.",
    description = "Adds CAS2 referral info to the CAS accommodation assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addCas2ReferralInfo(
    @Parameter(required = true) @PathVariable reference: Long,
    @RequestBody @Valid cas2ReferralInfoRequest: Cas2ReferralInfoRequest,
  ) = casAccommodationAssessmentService.addCas2ReferralInfo(reference, cas2ReferralInfoRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/cas/assessment/{reference}/outcome")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Adds outcome for the CAS accommodation assessment.",
    description = "Adds outcome for the CAS accommodation assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addOutcome(
    @Parameter(required = true) @PathVariable reference: Long,
    @RequestBody @Valid casAccommodationAssessmentOutcomeRequest: CasAccommodationAssessmentOutcomeRequest,
  ) = casAccommodationAssessmentService.addOutcome(reference, casAccommodationAssessmentOutcomeRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/cas/assessment/{reference}/address")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Add an address for the CAS accommodation assessment.",
    description = "Add an address for the CAS accommodation assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "CAS accommodation status info response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasAccommodationStatusInfoResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addAddress(
    @Parameter(required = true) @PathVariable reference: Long,
    @RequestBody @Valid casAccommodationAssessmentAddressRequest: CasAccommodationAssessmentAddressRequest,
  ) = casAccommodationAssessmentService.addAddress(reference, casAccommodationAssessmentAddressRequest, agentHolder.getAgentOrThrow())
}
