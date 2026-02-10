package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import lombok.Builder;

/**
 * Holds the breakdown of the fee calculation for a claim.
 *
 * @param fixedFee the fixed fee.
 * @param netProfitCost the net profit cost.
 * @param netDisbursments the net disbursements.
 * @param disbursementVat the disbursement VAT.
 * @param netCostOfCounsel the net cost of counsel.
 * @param travelCosts the travel costs.
 * @param waitingCosts the waiting costs.
 * @param adjournedHearingFee the adjourned hearing fee.
 * @param jrFormFilling the JR form filling.
 * @param detentionTravelAndWaitingCosts the detention travel and waiting costs.
 * @param cmrhTelephone the CMRH telephone costs.
 * @param cmrhOral the CMRH oral costs.
 * @param homeOfficeInterview the home office interview costs.
 * @param substantiveHearing the substantive hearing costs.
 * @param vat the VAT.
 * @param calculatedTotal the final calculated calculatedTotal.
 */
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
