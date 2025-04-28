package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.EvaluationStrategy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Policy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Question

val POLICY_1_0 = Policy(
  code = "1.0",
  eligibilityCriteria = listOf(
    Criterion(
      code = "sex-offender-register",
      name = "Sex offenders' register",
      question = Question(
        name = "requiredToSignSexOffenderRegister",
        text = "Is this person required to sign the sex offenders' register?",
        documentFailureReason = "you will be subject to sex offender registration on release",
      ),
    ),
    Criterion(
      code = "extended-sentence-for-violent-or-sexual-offences",
      name = "Extended Sentence for violent or sexual offences",
      question = Question(
        name = "servingExtendedSentenceForViolentOrSexualOffence",
        text = "Is this person serving an extended sentence for violent or sexual offences?",
        documentFailureReason = "you are serving an extended sentence",
      ),
    ),
    Criterion(
      code = "rotl-failure-to-return",
      name = "ROTL failure to return",
      question = Question(
        name = "rotlFailedToReturn",
        text = "Is this person serving a sentence for ROTL failure to return?",
        documentFailureReason = "you did not return from release on temporary licence (ROTL).",
      ),
    ),
    Criterion(
      code = "community-order-curfew-breach",
      name = "Community order curfew breach",
      question = Question(
        name = "communityOrderCurfewBreach",
        text = "Is this person serving a sentence for breaching a community order curfew?",
        documentFailureReason = "you breached your community order curfew",
      ),
    ),
    Criterion(
      code = "recalled-for-breaching-hdc-curfew",
      name = "Recalled for breaching HDC curfew",
      question = Question(
        name = "recalledForBreachingHdcCurfew",
        text = "Has this person been recalled for breaching their HDC curfew within 2 years of their current sentence date",
        documentFailureReason = "you previously broke home detention curfew conditions and were recalled to prison",
      ),
    ),
    Criterion(
      code = "deportation-orders",
      name = "Deportation Orders",
      questions = listOf(
        Question(
          name = "recommendedForDeportation",
          text = "Is this person a foreign national who has been recommended for deportation?",
          documentFailureReason = "the court recommended you should be deported from the UK",
        ),
        Question(
          name = "servedADecisionToDeport",
          text = "Is this person a foreign national who is liable to deportation and been served with a decision to deport?",
          documentFailureReason = "the Home Office recommended you should be deported from the UK",
        ),
      ),
    ),
    Criterion(
      code = "recalled-from-early-release-on-compassionate-grounds",
      name = "Recalled from early release on compassionate grounds",
      question = Question(
        name = "recalledFromEarlyReleaseOnCompassionateGrounds",
        text = "Is this person currently serving a recall from early release on compassionate grounds?",
        documentFailureReason = "you were recalled from early release on compassionate grounds",
      ),
    ),
    Criterion(
      code = "terrorism",
      name = "Terrorism",
      question = Question(
        name = "terroristSentenceOrOffending",
        text = "Is this person currently serving a sentence for terrorist or terrorist-connected offending?",
        documentFailureReason = "you are serving a sentence for a specified terrorist or terrorist connected offence",
      ),
    ),
    Criterion(
      code = "two-thirds-release",
      name = "Two-thirds release",
      questions = listOf(
        Question(
          name = "subjectToTwoThirdsReleaseCriteria",
          text = "Is this person serving an SDS+, DYOI+, or S250+ sentence subject to two-thirds release criteria?",
          documentFailureReason = "you are serving a sentence subject to two-thirds release criteria (SDS+, DYOI+ or S250+)",
        ),
        Question(
          name = "retrospectiveTwoThirdsReleaseCriteria",
          text = "Would this person be serving an SDS+, DYOI+ or S250+ sentence if they had been sentenced after the Police and Crime Sentencing Act 2022 was introduced?",
          documentFailureReason = "you are serving a sentence which would have been subject to two-thirds release if you had been sentenced today",
          hint = """
            <p>This applies if a sentence of:</p>
            <ul class="govuk-list govuk-list--bullet" style="margin-top: 0px; margin-bottom: 20px;">
              <li>seven years or more was given before April 1 2020 for adults or before June 28 2022 for children for any of the serious sexual or violent offences in parts 1 and 2 of schedule 15 </li>
              <li>between 4 and 7 years was given before 28 June 2022 for any of the serious sexual offences listed in Part 2 of schedule 15 or manslaughter, attempt/conspire/solicit/incite murder, and wounding with intent to do grievous bodily harm</li>
            </ul>
            """,
        ),
      ),
    ),
    Criterion(
      code = "section-244ZB-notice",
      name = "Section 244ZB notice",
      question = Question(
        name = "served244ZBNoticeInForce",
        text = "Has notice under section 244ZB been served that is still in force?",
        documentFailureReason = "the Secretary of State has referred your case to the Parole Board under section 244ZB of the Criminal Justice Act 2003",
      ),
    ),
    Criterion(
      code = "section-20b-release",
      name = "Section 20B release",
      question = Question(
        name = "schedule20BRelease",
        text = "Is this person's release governed by schedule 20B of the Criminal Justice Act 2003?",
        documentFailureReason = "you are subject to the release provisions for long-term prisoners set out in the Criminal Justice Act 1991 (Schedule 20B of the 2003 Act)",
      ),
    ),
  ),

  suitabilityCriteria = listOf(
    Criterion(
      code = "sexual-offending-history",
      name = "Sexual offending history",
      question = Question(
        name = "historyOfSexualOffending",
        text = "Does this person have a history of sexual-offending but is not required to sign the sex offender's register?",
        documentFailureReason = "of your conviction history",
      ),
    ),
    Criterion(
      code = "liable-to-deportation",
      name = "Liable to deportation",
      question = Question(
        name = "liableToDeportation",
        text = "Is this person a foreign national who is liable to deportation but not yet served a decision to deport?",
        documentFailureReason = "you are being considered for deportation",
      ),
    ),
    Criterion(
      code = "recalled-for-poor-behaviour-on-hdc",
      name = "Recalled for poor behaviour on hdc",
      question = Question(
        name = "poorBehaviourOnHdc",
        text = "Has this person been recalled for poor behaviour on HDC within 2 years of their current HDC date?",
        documentFailureReason = "you were recalled to prison for poor behaviour during your previous home detention curfew release",
      ),
    ),
    Criterion(
      code = "category-a",
      name = "Category A",
      question = Question(
        name = "categoryA",
        text = "Is this person category A?",
        documentFailureReason = "you are a category A prisoner",
      ),
    ),
    Criterion(
      code = "rosh-and-mappa",
      name = "RoSH and MAPPA",
      questions = listOf(
        Question(name = "highOrVeryHighRoSH", text = "Does this person have a high or very high risk of serious harm?", documentFailureReason = "you are at high risk of harm and subject to MAPPA level 2 or 3 management"),
        Question(name = "mappaLevel2Or3", text = "Are they MAPPA level 2 or 3?", documentFailureReason = "you are at high risk of harm and subject to MAPPA level 2 or 3 management"),
      ),
      evaluationStrategy = EvaluationStrategy.MET_IF_ANY_ARE_FALSE,
    ),
    Criterion(
      code = "presumed-unsuitable-offences",
      name = "Presumed unsuitable offences",
      question = Question(
        name = "presumedUnsuitableOffences",
        text = "Is this person serving a prison sentence for any of these categories of offence?",
        documentFailureReason = "of the type of offence you were convicted of",
        hint = """
         <ul id="sentences-list" class="govuk-list govuk-list--bullet">
            <li>Homicide</li>
            <li>Explosives</li>
            <li>Possession of an offensive weapon</li>
            <li>Possession of firearms with intent</li>
            <li>Cruelty to children</li>
            <li>Racially aggravated offences</li>
            <li>Stalking, harassment and other offences linked to domestic abuse</li>
            <li>Terrorist offences not described in section 247A(2) of the Criminal Justice Act 2003</li>
          </ul>
        """.trimIndent(),
      ),
    ),
    Criterion(
      code = "terrorist-offending-history",
      name = "Terrorist offending history",
      question = Question(
        name = "terroristOffendingHistory",
        text = "Does this person have a history of terrorist or terrorist connected offending?",
        documentFailureReason = "of your terrorist or terrorist connected offending history",
      ),
    ),
  ),
)
