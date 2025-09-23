package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import lombok.Builder;

/**
 * Holds information about fee calculation in a claim.
 *
 * @param totalValue the total value
 * @param adviceTime the advice time
 * @param travelTime the travel time
 * @param waitingTime the waiting time
 * @param netProfitCostsAmount the net profit costs amount
 * @param netDisbursementAmount the net disbursement amount
 * @param netCounselCostsAmount the net counsel costs amount
 * @param disbursementsVatAmount the disbursements VAT amount
 * @param travelWaitingCostsAmount the travel waiting costs amount
 * @param netWaitingCostsAmount the net waiting costs amount
 * @param isVatApplicable the VAT applicable flag
 * @param isLondonRate the London rate flag
 * @param adjournedHearingFeeAmount the adjourned hearing fee amount
 * @param costsDamagesRecoveredAmount the costs damages recovered amount
 * @param detentionTravelWaitingCostsAmount the detention travel waiting costs amount
 * @param jrFormFillingAmount the JR form filling amount
 */
@Builder
public record SubmissionClaimFeeCalculationDetails(
    BigDecimal totalValue,
    BigDecimal netProfitCostsAmount,
    BigDecimal netDisbursementAmount,
    BigDecimal netCounselCostsAmount,
    BigDecimal disbursementsVatAmount,
    BigDecimal travelWaitingCostsAmount,
    Integer adviceTime,
    Integer travelTime,
    Integer waitingTime,
    BigDecimal netWaitingCostsAmount,
    Boolean isVatApplicable,
    Boolean isLondonRate,
    Integer adjournedHearingFeeAmount,
    BigDecimal costsDamagesRecoveredAmount,
    BigDecimal detentionTravelWaitingCostsAmount,
    BigDecimal jrFormFillingAmount) {}
