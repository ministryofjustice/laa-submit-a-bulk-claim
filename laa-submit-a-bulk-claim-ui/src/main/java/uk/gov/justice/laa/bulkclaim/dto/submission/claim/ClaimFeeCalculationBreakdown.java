package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ClaimFeeCalculationBreakdown(
    BulkClaimCostItem fixedFee,
    BulkClaimCostItem netProfitCost,
    BulkClaimCostItem netDisbursments,
    BulkClaimCostItem disbursementVat,
    BulkClaimCostItem netCostOfCounsel,
    BulkClaimCostItem travelCosts, /* Crime Lower */
    BulkClaimCostItem waitingCosts, /* Crime Lower */
    BulkClaimCostItem travelAndWaitingCosts, /* Legal Help */
    BulkClaimCostItem adjournedHearingFee,
    BulkClaimCostItem jrFormFilling,
    BulkClaimCostItem detentionTravelAndWaitingCosts,
    BulkClaimCostItem cmrhTelephone,
    BulkClaimCostItem cmrhOral,
    BulkClaimCostItem homeOfficeInterview,
    BulkClaimCostItem substantiveHearing,
    BulkClaimCostItem vat,
    BigDecimal calculatedTotal) {}
