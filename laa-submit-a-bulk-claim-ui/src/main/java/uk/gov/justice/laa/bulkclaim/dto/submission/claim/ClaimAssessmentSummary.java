package uk.gov.justice.laa.bulkclaim.dto.submission.claim;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
 * @param detentionTravelAndWaitingCosts the detention travel and waiting costs.
 * @param adjournedHearingFee the adjourned hearing fee.
 * @param jrFormFilling the JR form filling.
 * @param cmrhTelephone the CMRH telephone costs.
 * @param cmrhOral the CMRH oral costs.
 * @param homeOfficeInterview the home office interview costs.
 * @param substantiveHearing the substantive hearing costs.
 * @param vatIndicator the VAT indicator.
 */
@Builder
public record ClaimAssessmentSummary(
        BigDecimal fixedFee,
        BigDecimal netProfitCost,
        BigDecimal netDisbursments,
        BigDecimal disbursementVat,
        BigDecimal netCostOfCounsel,
        BigDecimal travelCosts,
        BigDecimal waitingCosts,
        BigDecimal detentionTravelAndWaitingCosts,
        BigDecimal jrFormFilling,
        BigDecimal cmrhTelephone,
        BigDecimal cmrhOral,
        BigDecimal homeOfficeInterview,
        BigDecimal substantiveHearing,
        BigDecimal adjournedHearingFee,
        Boolean vatIndicator
) {}
