package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;

/**
 * Holds information about a claim in a submission.
 *
 * @param id
 * @param status
 * @param scheduleReference
 * @param lineNumber
 * @param caseReferenceNumber
 * @param uniqueFileNumber
 * @param caseStartDate
 * @param caseConcludedDate
 * @param matterTypeCode
 * @param crimeMatterTypeCode
 * @param feeSchemeCode
 * @param feeCode
 * @param procurementAreaCode
 * @param accessPointCode
 * @param deliveryLocation
 * @param representationOrderDate
 * @param suspectsDefendantsCount
 * @param policeStationCourtAttendancesCount
 * @param policeStationCourtPrisonId
 * @param dsccNumber
 * @param maatId
 * @param prisonLawPriorApprovalNumber
 * @param isDutySolicitor
 * @param isYouthCourt
 * @param schemeId
 * @param mediationSessionsCount
 * @param mediationTimeMinutes
 * @param outreachLocation
 * @param referralSource
 * @param totalValue
 * @param clientForename
 * @param clientSurname
 * @param clientDateOfBirth
 * @param uniqueClientNumber
 * @param clientPostcode
 * @param genderCode
 * @param ethnicityCode
 * @param disabilityCode
 * @param isLegallyAided
 * @param clientTypeCode
 * @param homeOfficeClientNumber
 * @param claReferenceNumber
 * @param claExemptionCode
 * @param client2Forename
 * @param client2Surname
 * @param client2DateOfBirth
 * @param client2Ucn
 * @param client2Postcode
 * @param client2GenderCode
 * @param client2EthnicityCode
 * @param client2DisabilityCode
 * @param client2IsLegallyAided
 * @param caseId
 * @param uniqueCaseId
 * @param caseStageCode
 * @param stageReachedCode
 * @param standardFeeCategoryCode
 * @param outcomeCode
 * @param designatedAccreditedRepresentativeCode
 * @param isPostalApplicationAccepted
 * @param isClient2PostalApplicationAccepted
 * @param mentalHealthTribunalReference
 * @param isNrmAdvice
 * @param followOnWork
 * @param transferDate
 * @param exemptionCriteriaSatisfied
 * @param exceptionalCaseFundingReference
 * @param isLegacyCase
 * @param adviceTime
 * @param travelTime
 * @param waitingTime
 * @param netProfitCostsAmount
 * @param netDisbursementAmount
 * @param netCounselCostsAmount
 * @param disbursementsVatAmount
 * @param travelWaitingCostsAmount
 * @param netWaitingCostsAmount
 * @param isVatApplicable
 * @param isToleranceApplicable
 * @param priorAuthorityReference
 * @param isLondonRate
 * @param adjournedHearingFeeAmount
 * @param isAdditionalTravelPayment
 * @param costsDamagesRecoveredAmount
 * @param meetingsAttendedCode
 * @param detentionTravelWaitingCostsAmount
 * @param jrFormFillingAmount
 * @param isEligibleClient
 * @param courtLocationCode
 * @param adviceTypeCode
 * @param medicalReportsCount
 * @param isIrcSurgery
 * @param surgeryDate
 * @param surgeryClientsCount
 * @param surgeryMattersCount
 * @param cmrhOralCount
 * @param cmrhTelephoneCount
 * @param aitHearingCentreCode
 * @param isSubstantiveHearing
 * @param hoInterview
 * @param localAuthorityNumber
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
    Integer adviceTime,
    Integer travelTime,
    Integer waitingTime,
    BigDecimal netProfitCostsAmount,
    BigDecimal netDisbursementAmount,
    BigDecimal netCounselCostsAmount,
    BigDecimal disbursementsVatAmount,
    BigDecimal travelWaitingCostsAmount,
    BigDecimal netWaitingCostsAmount,
    Boolean isVatApplicable,
    Boolean isToleranceApplicable,
    String priorAuthorityReference,
    Boolean isLondonRate,
    Integer adjournedHearingFeeAmount,
    Boolean isAdditionalTravelPayment,
    BigDecimal costsDamagesRecoveredAmount,
    String meetingsAttendedCode,
    BigDecimal detentionTravelWaitingCostsAmount,
    BigDecimal jrFormFillingAmount,
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
