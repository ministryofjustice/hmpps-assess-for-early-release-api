package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Resident
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PREPARE_FOR_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PROBATION_COM
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.RiskManagementDecisionAnswers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.RiskManagementDecisionTaskAnswers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.PostponeCaseRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateCom
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ResultType.FAILED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ResultType.PASSED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.PolicyVersion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonApiUserDetail
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.DeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.Name
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.Provider
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.Team
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {

  const val PRISON_NUMBER = "A1234AA"
  const val BOOKING_ID = "123"
  const val FORENAME = "Bob"
  const val SURNAME = "Smith"
  const val PRISON_ID = "AFG"
  const val PRISON_NAME = "Birmingham (HMP)"
  const val STAFF_CODE = "STAFF1"
  const val ADDRESS_REQUEST_ID = 1L
  const val RESIDENTIAL_CHECK_TASK_CODE = "assess-this-persons-risk"
  val PRISON_CA_AGENT = AgentDto("prisonUser", fullName = "prison user", role = PRISON_CA, onBehalfOf = "KXE")
  val PROBATION_COM_AGENT = AgentDto("probationUser", fullName = "probation user", role = PROBATION_COM, onBehalfOf = "ABC123")
  val criterion = POLICY_1_0.eligibilityCriteria[0]
  private val question = criterion.questions.first()
  val answers = mapOf(question.name to false)

  fun anOffender(hdced: LocalDate = LocalDate.now().plusDays(7), sentenceStartDate: LocalDate? = null): Offender {
    val offender = Offender(
      id = 1,
      bookingId = BOOKING_ID.toLong(),
      prisonNumber = PRISON_NUMBER,
      prisonId = PRISON_ID,
      forename = FORENAME,
      surname = SURNAME,
      dateOfBirth = LocalDate.of(1981, 5, 23),
      hdced = hdced,
      sentenceStartDate = sentenceStartDate,
    )
    offender.assessments.add(anAssessment(offender))
    return offender
  }

  val saveResidentialChecksTaskAnswersRequest =
    SaveResidentialChecksTaskAnswersRequest(
      taskCode = "address-details-and-informed-consent",
      answers = mapOf(
        "electricitySupply" to "true",
        "visitedAddress" to "I_HAVE_NOT_VISITED_THE_ADDRESS_BUT_I_HAVE_SPOKEN_TO_THE_MAIN_OCCUPIER",
        "mainOccupierConsentGiven" to "true",
      ),
      agentDto = PROBATION_COM_AGENT,
    )

  val anPostponeCaseRequest = PostponeCaseRequest(
    reasonTypes =
    LinkedHashSet(
      listOf(
        PostponeCaseReasonType.ON_REMAND,
        PostponeCaseReasonType.COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY,
      ),
    ),
    agentDto = PRISON_CA_AGENT,
  )

  fun anAssessment(offender: Offender, status: AssessmentStatus = NOT_STARTED): Assessment = Assessment(offender = offender, status = status, policyVersion = PolicyService.CURRENT_POLICY_VERSION.code)

  fun aPrisonerSearchPrisoner(hdced: LocalDate? = null, sentenceStartDate: LocalDate? = null) = PrisonerSearchPrisoner(
    PRISON_NUMBER,
    bookingId = BOOKING_ID,
    hdced,
    sentenceStartDate = sentenceStartDate,
    firstName = FORENAME,
    lastName = SURNAME,
    dateOfBirth = LocalDate.of(1981, 5, 23),
    prisonId = PRISON_ID,
    cellLocation = "A-1-002",
    mostSeriousOffence = "Robbery",
    prisonName = PRISON_NAME,
  )

  fun anAssessmentSummary() = AssessmentSummary(
    forename = FORENAME,
    surname = SURNAME,
    dateOfBirth = LocalDate.of(1981, 5, 23),
    prisonNumber = PRISON_NUMBER,
    hdced = LocalDate.of(2020, 10, 25),
    crd = LocalDate.of(2022, 3, 21),
    location = PRISON_NAME,
    status = NOT_STARTED,
    policyVersion = "1.0",
    cellLocation = "A-1-002",
    mainOffense = "Robbery",
    tasks = mapOf(
      PRISON_CA to listOf(
        TaskProgress(name = ASSESS_ELIGIBILITY, progress = READY_TO_START),
        TaskProgress(name = ENTER_CURFEW_ADDRESS, progress = LOCKED),
        TaskProgress(name = REVIEW_APPLICATION_AND_SEND_FOR_DECISION, progress = LOCKED),
        TaskProgress(name = PREPARE_FOR_RELEASE, progress = LOCKED),
        TaskProgress(name = PRINT_LICENCE, progress = LOCKED),
      ),
    ),
  )

  fun anEligibilityCheckDetails(n: Int) = EligibilityCriterionProgress(
    code = "code-$n",
    taskName = "task-$n",
    questions = listOf(Question("question-$n", answer = true)),
    status = ELIGIBLE,
    agent = PRISON_CA_AGENT,
    lastUpdated = LocalDate.now(),
  )

  fun anSuitabilityCheckDetails(n: Int) = SuitabilityCriterionProgress(
    code = "code-$n",
    taskName = "task-$n",
    questions = listOf(Question("question-$n", answer = true)),
    status = SUITABLE,
    agent = PRISON_CA_AGENT,
    lastUpdated = LocalDate.now(),
  )

  enum class ResultType { PASSED, FAILED }

  interface Progress {
    fun contains(index: Int): Boolean
    operator fun get(index: Int): ResultType?

    companion object {
      fun specifyByIndex(vararg results: Pair<Int, ResultType>): Progress = object : Progress {
        private val map = results.toMap()
        override fun contains(index: Int) = map.containsKey(index)
        override fun get(index: Int) = map[index]
      }

      fun none(): Progress = object : Progress {
        override fun contains(index: Int) = false
        override fun get(index: Int) = null
      }

      fun allSuccessful(): Progress = object : Progress {
        override fun contains(index: Int) = true
        override fun get(index: Int) = PASSED
      }
    }
  }

  fun anAssessmentWithSomeProgress(
    status: AssessmentStatus,
    previousStatus: AssessmentStatus? = null,
    eligibilityProgress: Progress = Progress.none(),
    suitabilityProgress: Progress = Progress.none(),
  ): AssessmentWithEligibilityProgress {
    val offender = anOffender()
    val assessment = offender.currentAssessment()
    assessment.status = status
    assessment.previousStatus = previousStatus
    assessment.eligibilityCheckResults.clear()
    assessment.eligibilityCheckResults.addAll(
      POLICY_1_0.eligibilityCriteria
        .flatMapIndexed { i, criterion ->
          listOfNotNull(
            when {
              !eligibilityProgress.contains(i) -> null
              eligibilityProgress[i] == PASSED -> criterion.toPassedResult(ELIGIBILITY, assessment)
              eligibilityProgress[i] == FAILED -> criterion.toFailedResult(ELIGIBILITY, assessment)
              else -> null
            },
          )
        } +
        POLICY_1_0.suitabilityCriteria
          .flatMapIndexed { i, criterion ->
            listOfNotNull(
              when {
                !suitabilityProgress.contains(i) -> null
                suitabilityProgress[i] == PASSED -> criterion.toPassedResult(SUITABILITY, assessment)
                suitabilityProgress[i] == FAILED -> criterion.toFailedResult(SUITABILITY, assessment)
                else -> null
              },
            )
          },
    )

    return AssessmentWithEligibilityProgress(
      policy = POLICY_1_0,
      assessmentEntity = assessment,
    )
  }

  fun anAssessmentWithNoProgress() = anAssessmentWithSomeProgress(NOT_STARTED, null, Progress.none(), Progress.none())

  fun anAssessmentWithCompleteEligibilityChecks(status: AssessmentStatus, previousStatus: AssessmentStatus? = null) = anAssessmentWithSomeProgress(status, previousStatus, Progress.allSuccessful(), Progress.allSuccessful())

  private fun Criterion.toPassedResult(
    type: CriterionType,
    assessment: Assessment,
  ): EligibilityCheckResult = EligibilityCheckResult(
    assessment = assessment,
    criterionCode = this.code,
    criterionType = type,
    criterionMet = true,
    id = 1,
    criterionVersion = POLICY_1_0.code,
    questionAnswers = this.questions.associate { it.name to true },
    agent = PRISON_CA_AGENT.toEntity(),
  )

  private fun Criterion.toFailedResult(
    type: CriterionType,
    assessment: Assessment,
  ): EligibilityCheckResult = EligibilityCheckResult(
    assessment = assessment,
    criterionCode = this.code,
    criterionType = type,
    criterionMet = false,
    id = 1,
    criterionVersion = POLICY_1_0.code,
    questionAnswers = this.questions.associate { it.name to false },
    agent = PRISON_CA_AGENT.toEntity(),
  )

  private fun anAddress() = Address(
    uprn = "200010019924",
    firstLine = "Langley Road",
    secondLine = "Kington Langley",
    town = "Chippers",
    county = "Wiltshire",
    postcode = "SN118TK",
    country = "England",
    xCoordinate = 401003.0,
    yCoordinate = 154111.0,
    addressLastUpdated = LocalDate.of(2022, 3, 21),
  )

  private fun residents() = mutableSetOf(
    Resident(
      id = 1,
      forename = "Langley",
      surname = "Road",
      phoneNumber = "07768967676",
      relation = "Father",
      dateOfBirth = LocalDate.of(1989, 3, 21),
      age = 32,
      isMainResident = true,
      isOffender = false,
      standardAddressCheckRequest = StandardAddressCheckRequest(
        dateRequested = LocalDateTime.of(2023, 6, 16, 11, 28),
        preferencePriority = AddressPreferencePriority.FIRST,
        assessment = anOffender().currentAssessment(),
        address = anAddress(),
      ),
    ),
  )

  fun aStandardAddressCheckRequest() = StandardAddressCheckRequest(
    dateRequested = LocalDateTime.of(2023, 6, 16, 11, 28),
    preferencePriority = AddressPreferencePriority.FIRST,
    assessment = anOffender().currentAssessment(),
    address = anAddress(),
    residents = residents(),
  )

  fun aCasCheckRequest() = CasCheckRequest(
    dateRequested = LocalDateTime.of(2024, 9, 7, 15, 19),
    preferencePriority = AddressPreferencePriority.FIRST,
    assessment = anOffender().currentAssessment(),
    allocatedAddress = anAddress(),
  )

  fun aDeliusOffenderManager() = DeliusOffenderManager(
    id = 1,
    code = "staff-code",
    Name(forename = "forename", surname = "surname"),
    team = Team(code = "team 1", "N55LAU"),
    provider = Provider(code = "N03", description = "Midlands"),
    username = "username",
  )

  fun aCommunityOffenderManager(deliusOffenderManager: DeliusOffenderManager = aDeliusOffenderManager()) = CommunityOffenderManager(
    staffCode = deliusOffenderManager.code,
    username = deliusOffenderManager.username,
    email = deliusOffenderManager.email,
    forename = deliusOffenderManager.name.forename,
    surname = deliusOffenderManager.name.surname,
  )

  fun aUpdateCom(deliusOffenderManager: DeliusOffenderManager) = deliusOffenderManager.username?.let {
    UpdateCom(
      staffCode = deliusOffenderManager.code,
      staffUsername = it,
      staffEmail = deliusOffenderManager.email,
      forename = deliusOffenderManager.name.forename,
      surname = deliusOffenderManager.name.surname,
    )
  }

  private fun aRiskManagementDecisionAnswers(): RiskManagementDecisionAnswers = RiskManagementDecisionAnswers(
    canOffenderBeManagedSafely = true,
    informationToSupportDecision = "reason",
    riskManagementPlanningActionsNeeded = false,
  )

  fun aRiskManagementDecisionTaskAnswers(criterionMet: Boolean, answers: RiskManagementDecisionAnswers = aRiskManagementDecisionAnswers()): RiskManagementDecisionTaskAnswers = RiskManagementDecisionTaskAnswers(
    id = 1,
    addressCheckRequest = aStandardAddressCheckRequest(),
    criterionMet = criterionMet,
    taskVersion = PolicyVersion.V1.name,
    answers = answers,
  )

  fun aPrisonApiUserDetails(): PrisonApiUserDetail = PrisonApiUserDetail(
    staffId = 8103,
    username = "STAFF1",
    firstName = "Gaz",
    lastName = "Lyndsay",
    activeCaseLoadId = "LEI",
    accountStatus = "ACTIVE",
    lockDate = LocalDateTime.of(2028, 3, 21, 11, 28),
    active = true,
  )
}
