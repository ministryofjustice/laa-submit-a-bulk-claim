package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;

/**
 * Holds information about a claim in a submission.
 *
 * @param id the UUID ID for the claim
 * @param status the status of the claim
 * @param scheduleReference the schedule reference
 * @param lineNumber the line number
 * @param caseReferenceNumber the case reference number
 * @param uniqueFileNumber the unique file number
 * @param caseStartDate the case start date
 * @param caseConcludedDate the case concluded date
 * @param matterTypeCode the matter type code
 * @param crimeMatterTypeCode the crime matter type code
 * @param feeSchemeCode the fee scheme code
 * @param feeCode the fee code
 * @param procurementAreaCode the procurement area code
 * @param accessPointCode the access point code
 * @param deliveryLocation the delivery location
 * @param representationOrderDate the representation order date
 * @param suspectsDefendantsCount the suspects defendants count
 * @param policeStationCourtAttendancesCount the police station court attendances count
 * @param policeStationCourtPrisonId the police station court prison id
 * @param dsccNumber the DSCC number
 * @param maatId the MAAT id
 * @param prisonLawPriorApprovalNumber the prison law prior approval number
 * @param isDutySolicitor the duty solicitor flag
 * @param isYouthCourt the youth court flag
 * @param schemeId the scheme id
 * @param mediationSessionsCount the mediation sessions count
 * @param mediationTimeMinutes the mediation time minutes
 * @param outreachLocation the outreach location
 * @param referralSource the referral source
 * @param totalValue the calculatedTotal value
 * @param clientForename the client forename
 * @param clientSurname the client surname
 * @param clientDateOfBirth the client date of birth
 * @param uniqueClientNumber the unique client number
 * @param clientPostcode the client postcode
 * @param genderCode the gender code
 * @param ethnicityCode the ethnicity code
 * @param disabilityCode the disability code
 * @param isLegallyAided the legally aided flag
 * @param clientTypeCode the client type code
 * @param homeOfficeClientNumber the home office client number
 * @param claReferenceNumber the CLA reference number
 * @param claExemptionCode the CLA exemption code
 * @param client2Forename the second client's forename
 * @param client2Surname the second client's surname
 * @param client2DateOfBirth the second client's date of birth
 * @param client2Ucn the second client's UCN
 * @param client2Postcode the second client's postcode
 * @param client2GenderCode the second client's gender code
 * @param client2EthnicityCode the second client's ethnicity code
 * @param client2DisabilityCode the second client's disability code
 * @param client2IsLegallyAided the second client's legally aided flag
 * @param caseId the case id
 * @param uniqueCaseId the unique case id
 * @param caseStageCode the case stage code
 * @param stageReachedCode the stage reached code
 * @param standardFeeCategoryCode the standard fee category code
 * @param outcomeCode the outcome code
 * @param designatedAccreditedRepresentativeCode the designated accredited representative code
 * @param isPostalApplicationAccepted the postal application accepted flag
 * @param isClient2PostalApplicationAccepted the second client's postal application accepted flag
 * @param mentalHealthTribunalReference the mental health tribunal reference
 * @param isNrmAdvice the NRM advice flag
 * @param followOnWork the follow on work
 * @param transferDate the transfer date
 * @param exemptionCriteriaSatisfied the exemption criteria satisfied
 * @param exceptionalCaseFundingReference the exceptional case funding reference
 * @param isLegacyCase the legacy case flag
 * @param isToleranceApplicable the tolerance applicable flag
 * @param priorAuthorityReference the prior authority reference
 * @param isAdditionalTravelPayment the additional travel payment flag
 * @param meetingsAttendedCode the meetings attended code
 * @param isEligibleClient the eligible client flag
 * @param courtLocationCode the court location code
 * @param adviceTypeCode the advice type code
 * @param medicalReportsCount the medical reports count
 * @param isIrcSurgery the IRC surgery flag
 * @param surgeryDate the surgery date
 * @param surgeryClientsCount the surgery clients count
 * @param surgeryMattersCount the surgery matters count
 * @param cmrhOralCount the CMRH oral count
 * @param cmrhTelephoneCount the CMRH telephone count
 * @param aitHearingCentreCode the AIT hearing centre code
 * @param isSubstantiveHearing the substantive hearing flag
 * @param hoInterview the HO interview
 * @param localAuthorityNumber the local authority number
 */
@Builder
public record SubmissionClaimDetails(
    UUID id,
    ClaimStatus status,
    String scheduleReference,
    Integer lineNumber,
    String caseReferenceNumber,
    String uniqueFileNumber,
    LocalDate caseStartDate,
    LocalDate caseConcludedDate,
    String matterTypeCode,
    String crimeMatterTypeCode,
    String feeSchemeCode,
    String feeCode,
    String procurementAreaCode,
    String accessPointCode,
    String deliveryLocation,
    LocalDate representationOrderDate,
    Integer suspectsDefendantsCount,
    Integer policeStationCourtAttendancesCount,
    String policeStationCourtPrisonId,
    String dsccNumber,
    String maatId,
    String prisonLawPriorApprovalNumber,
    Boolean isDutySolicitor,
    Boolean isYouthCourt,
    String schemeId,
    Integer mediationSessionsCount,
    Integer mediationTimeMinutes,
    String outreachLocation,
    String referralSource,
    BigDecimal totalValue,
    String clientForename,
    String clientSurname,
    LocalDate clientDateOfBirth,
    String uniqueClientNumber,
    String clientPostcode,
    String genderCode,
    String ethnicityCode,
    String disabilityCode,
    Boolean isLegallyAided,
    String clientTypeCode,
    String homeOfficeClientNumber,
    String claReferenceNumber,
    String claExemptionCode,
    String client2Forename,
    String client2Surname,
    LocalDate client2DateOfBirth,
    String client2Ucn,
    String client2Postcode,
    String client2GenderCode,
    String client2EthnicityCode,
    String client2DisabilityCode,
    Boolean client2IsLegallyAided,
    String caseId,
    String uniqueCaseId,
    String caseStageCode,
    String stageReachedCode,
    String standardFeeCategoryCode,
    String outcomeCode,
    String designatedAccreditedRepresentativeCode,
    Boolean isPostalApplicationAccepted,
    Boolean isClient2PostalApplicationAccepted,
    String mentalHealthTribunalReference,
    Boolean isNrmAdvice,
    String followOnWork,
    LocalDate transferDate,
    String exemptionCriteriaSatisfied,
    String exceptionalCaseFundingReference,
    Boolean isLegacyCase,
    Boolean isToleranceApplicable,
    String priorAuthorityReference,
    Boolean isAdditionalTravelPayment,
    String meetingsAttendedCode,
    Boolean isEligibleClient,
    String courtLocationCode,
    String adviceTypeCode,
    Integer medicalReportsCount,
    Boolean isIrcSurgery,
    LocalDate surgeryDate,
    Integer surgeryClientsCount,
    Integer surgeryMattersCount,
    Integer cmrhOralCount,
    Integer cmrhTelephoneCount,
    String aitHearingCentreCode,
    Boolean isSubstantiveHearing,
    Integer hoInterview,
    String localAuthorityNumber) {}
