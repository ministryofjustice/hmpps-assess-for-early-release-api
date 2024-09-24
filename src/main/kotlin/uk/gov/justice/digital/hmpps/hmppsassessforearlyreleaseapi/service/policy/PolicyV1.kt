package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Check
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Policy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Question

val POLICY_1_0 = Policy(
  code = "1.0",
  eligibilityCriteria = listOf(
    Check(
      code = "sex-offender-register",
      name = "Sex offenders' register",
      question = Question(
        name = "requiredToSignSexOffenderRegister",
        text = "Is this person required to sign the sex offenders' register?",
      ),
    ),
    Check(
      code = "extended-sentence-for-violent-or-sexual-offences",
      name = "Extended Sentence for violent or sexual offences",
      question = Question(
        name = "servingExtendedSentenceForViolentOrSexualOffence",
        text = "Is this person serving an extended sentence for violent or sexual offences?",
      ),
    ),
    Check(
      code = "rotl-failure-to-return",
      name = "ROTL failure to return",
      question = Question(
        name = "rotlFailedToReturn",
        text = "Is this person serving a sentence for ROTL failure to return?",
      ),
    ),
    Check(
      code = "community-order-curfew-breach",
      name = "Community order curfew breach",
      question = Question(
        name = "communityOrderCurfewBreach",
        text = "Is this person serving a sentence for breaching a community order curfew?",
      ),
    ),
    Check(
      code = "recalled-for-breaching-hdc-curfew",
      name = "Recalled for breaching HDC curfew",
      question = Question(
        name = "recalledForBreachingHdcCurfew",
        text = "Has this person been recalled for breaching their HDC curfew within 2 years of their current sentence date",
      ),
    ),
    Check(
      code = "deportation-orders",
      name = "Deportation Orders",
      questions = listOf(
        Question(
          name = "recommendedForDeportation",
          text = "Is this person a foreign national who has been recommended for deportation?",
        ),
        Question(
          name = "servedADecisionToDeport",
          text = "Is this person a foreign national who is liable to deportation and been served with a decision to deport?",
        ),
      ),
    ),
    Check(
      code = "recalled-from-early-release-on-compassionate-grounds",
      name = "Recalled from early release on compassionate grounds",
      question = Question(
        name = "recalledFromEarlyReleaseOnCompassionateGrounds",
        text = "Is this person currently serving a recall from early release on compassionate grounds?",
      ),
    ),
    Check(
      code = "terrorism",
      name = "Terrorism",
      question = Question(
        name = "terroristSentenceOrOffending",
        text = "Is this person currently serving a sentence for terrorist or terrorist-connected offending?",
      ),
    ),
    Check(
      code = "two-thirds-release",
      name = "Two-thirds release",
      questions = listOf(
        Question(
          name = "subjectToTwoThirdsReleaseCriteria",
          text = "Is this person serving an SDS+, DYOI+, or S250+ sentence subject to two-thirds release criteria?",
        ),
        Question(
          name = "retrospectiveTwoThirdsReleaseCriteria",
          text = "Would this person be serving an SDS+, DYOI+ or S250+ sentence if they had been sentenced after the Police and Crime Sentencing Act 2022 was introduced?",
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
    Check(
      code = "section-244ZB-notice",
      name = "Section 244ZB notice",
      question = Question(
        name = "served244ZBNoticeInForce",
        text = "Has notice under section 244ZB been served that is still in force?",
      ),
    ),
    Check(
      code = "section-20b-release",
      name = "Section 20B release",
      question = Question(
        name = "schedule20BRelease",
        text = "Is this person's release governed by schedule 20B of the Criminal Justice Act 2003?",
      ),
    ),
  ),

  suitabilityCriteria = listOf(
    Check(
      code = "sexual-offending-history",
      name = "Sexual offending history",
      question = Question(
        name = "historyOfSexualOffending",
        text = "Does this person have a history of sexual-offending but is not required to sign the sex offender's register?",
      ),
    ),
    Check(
      code = "liable-to-deportation",
      name = "Liable to deportation",
      question = Question(
        name = "liableToDeportation",
        text = "Is this person a foreign national who is liable to deportation but not yet served a decision to deport?",
      ),
    ),
    Check(
      code = "recalled-for-poor-behaviour-on-hdc",
      name = "Recalled for poor behaviour on hdc",
      question = Question(
        name = "poorBehaviourOnHdc",
        text = "Has this person been recalled for poor behaviour on HDC within 2 years of their current HDC date?",
      ),
    ),
    Check(
      code = "category-a",
      name = "Category A",
      question = Question(name = "categoryA", text = "Is this person category A?"),
    ),
    Check(
      code = "rosh-and-mappa",
      name = "RoSH and MAPPA",
      questions = listOf(
        Question(name = "highOrVeryHighRoSH", text = "Does this person have a high or very high risk of serious harm?"),
        Question(name = "mappaLevel2Or3", text = "Are they MAPPA level 2 or 3?"),
      ),
    ),
    Check(
      code = "presumed-unsuitable-offences",
      name = "Presumed unsuitable offences",
      question = Question(
        name = "presumedUnsuitableOffences",
        text = "Is this person serving a prison sentence for any of these categories of offence?",
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
    Check(
      code = "terrorist-offending-history",
      name = "Terrorist offending history",
      question = Question(
        name = "terroristOffendingHistory",
        text = "Does this person have a history of terrorist or terrorist connected offending?",
      ),
    ),
  ),
)
