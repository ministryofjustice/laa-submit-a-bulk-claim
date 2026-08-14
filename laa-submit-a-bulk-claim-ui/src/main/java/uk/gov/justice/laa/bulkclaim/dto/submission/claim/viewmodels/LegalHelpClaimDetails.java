package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Builder
public record LegalHelpClaimDetails(
    // Page header / summary fields
    String clientForename,
    String clientSurname,
    String uniqueFileNumber,
    String officeCode,
    OffsetDateTime dateSubmitted,
    AreaOfLaw areaOfLaw,
    String categoryOfLaw,
    String feeCode,
    String feeCodeDescription,
    String matterTypeCodeOne,
    String matterTypeCodeTwo,
    String caseStartDate,
    String caseConcludedDate,
    Boolean escapeCase,

    // Values - Reported column
    BigDecimal reportedProfitCosts,
    BigDecimal reportedDisbursements,
    BigDecimal reportedDisbursementsVat,
    BigDecimal reportedTravelAndWaitingCosts,
    Boolean reportedVatApplicable,
    Boolean reportedLondonRateIndicator,

    // Values - Initial calculated column
    BigDecimal initialCalculatedFixedFee,
    BigDecimal initialCalculatedProfitCosts,
    BigDecimal initialCalculatedDisbursements,
    BigDecimal initialCalculatedDisbursementsVat,
    BigDecimal initialCalculatedCounselsCosts,
    BigDecimal initialCalculatedTravelAndWaitingCosts,
    BigDecimal initialCalculatedDetentionTravelWaitingCosts,
    BigDecimal initialCalculatedJrFormFilling,
    BigDecimal initialCalculatedAdjournedHearingFee,
    BigDecimal initialCalculatedCmrhOral,
    BigDecimal initialCalculatedCmrhTelephone,
    BigDecimal initialCalculatedHomeOfficeInterview,
    BigDecimal initialCalculatedSubstantiveHearing,
    Boolean initialCalculatedVatIndicator,

    // Total allowed value
    BigDecimal initialCalculatedTotalVat,
    BigDecimal initialCalculatedTotalIncludingVat) {

  public String clientName() {
    return "%s %s".formatted(clientForename, clientSurname);
  }
}
