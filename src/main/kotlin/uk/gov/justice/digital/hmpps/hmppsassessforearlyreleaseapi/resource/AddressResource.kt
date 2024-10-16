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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.AddressService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class AddressResource(private val addressService: AddressService) {
  @GetMapping("/addresses")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns addresses that match the postcode parameter",
    description = "Returns addresses that match the postcode parameter",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns addresses matching the supplied postcode",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AddressSummary::class),
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
  fun getAddressesForPostcode(@RequestParam(name = "postcode") postcode: String) =
    addressService.getAddressesForPostcode(postcode)

  @GetMapping("/address/uprn/{uprn}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Gets an address by it's UPRN",
    description = "Gets an address by it's UPRN",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the address with the provided UPRN",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AddressSummary::class),
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
  fun getAddressForUprn(@PathVariable(name = "uprn") uprn: String) =
    addressService.getAddressForUprn(uprn)

  @PostMapping("/offender/{prisonNumber}/current-assessment/standard-address-check-request")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Adds a standard address check request for an offender.",
    description = "Adds a standard address check request for an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "The standard address check request has been added to the offender's current assessment.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = StandardAddressCheckRequestSummary::class),
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
  fun addStandardAddressCheckRequest(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Valid @RequestBody addStandardAddressCheckRequest: AddStandardAddressCheckRequest,
  ) = addressService.addStandardAddressCheckRequest(prisonNumber, addStandardAddressCheckRequest)

  @GetMapping("/offender/{prisonNumber}/current-assessment/standard-address-check-request/{requestId}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Gets a standard address check request by it's request id.",
    description = "Gets a standard address check request by it's request id.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the standard address check request with the specified request id",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = StandardAddressCheckRequestSummary::class),
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
  fun getStandardAddressCheckRequest(@PathVariable(name = "prisonNumber") prisonNumber: String, @PathVariable(name = "requestId") requestId: Long) =
    addressService.getStandardAddressCheckRequest(prisonNumber, requestId)

  @PostMapping("/offender/{prisonNumber}/current-assessment/cas-check-request")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Adds a CAS check request for an offender.",
    description = "Adds a CAS check request for an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "The CAS check request has been added to the current assessment for the offender.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CasCheckRequestSummary::class),
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
  fun addCasCheckRequest(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Valid @RequestBody addCasCheckRequest: AddCasCheckRequest,
  ) = addressService.addCasCheckRequest(prisonNumber, addCasCheckRequest)

  @PostMapping("/offender/{prisonNumber}/current-assessment/standard-address-check-request/{requestId}/resident")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Adds a resident to standard address check request for an assessment.",
    description = "Adds a resident to a standard address check request for an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "The resident has been added to the standard address check request.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ResidentSummary::class),
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
  fun addStandardAddressCheckRequestResident(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
    @Valid @RequestBody addResidentRequest: AddResidentRequest,
  ) = addressService.addResident(prisonNumber, requestId, addResidentRequest)
}
