package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Builder
public record CrimeLowerClaimDetails(
    // Page header / summary fields
    String clientForename,
    String clientSurname,
    String uniqueFileNumber,
    String officeCode,
    OffsetDateTime dateSubmitted,
    AreaOfLaw areaOfLaw,
    String feeCode,
    String feeCodeDescription,
    String matterTypeCode,
    String representationOrderDate,
    String stageReachedCode,
    String outcomeCode,
    String caseConcludedDate,
    Boolean escapeCase,

    // Values - Reported column
    BigDecimal reportedProfitCosts,
    BigDecimal reportedDisbursements,
    BigDecimal reportedDisbursementsVat,
    BigDecimal reportedTravelCosts,
    BigDecimal reportedWaitingCosts,
    Boolean reportedVatApplicable,

    // Values - Initial calculated column
    BigDecimal initialCalculatedFixedFee,
    BigDecimal initialCalculatedProfitCosts,
    BigDecimal initialCalculatedDisbursements,
    BigDecimal initialCalculatedDisbursementsVat,
    BigDecimal initialCalculatedTravelCosts,
    BigDecimal initialCalculatedWaitingCosts,
    Boolean initialCalculatedVatIndicator,

    // Total allowed value
    BigDecimal initialCalculatedTotalVat,
    BigDecimal initialCalculatedTotalIncludingVat) {
  public String clientName() {
    return "%s %s".formatted(clientForename, clientSurname);
  }
}
