package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;

/**
 * Holds details about the costs of a claim in a submission.
 *
 * @param netProfitCostsAmount the profit costs of the claim
 * @param netDisbursementAmount the disbursements of the claim including VAT
 * @param disbursementsVatAmount the VAT amount of the disbursements
 * @param netCounselCostsAmount the counsel costs of the claim
 * @param travelWaitingCostsAmount the travel waiting costs of the claim
 * @param netWaitingCostsAmount the waiting costs of the claim
 * @param claimValue the claim value
 * @author Jamie Briggs
 */
public record SubmissionClaimRowCostsDetails(
    BigDecimal netProfitCostsAmount,
    BigDecimal netDisbursementAmount,
    BigDecimal disbursementsVatAmount,
    BigDecimal netCounselCostsAmount,
    BigDecimal travelWaitingCostsAmount,
    BigDecimal netWaitingCostsAmount,
    BigDecimal claimValue) {

  /**
   * Returns a sum of the counsel costs, travel waiting costs and waiting costs. This excludes the
   * net profit and disbursement costs.
   *
   * @return the sum of the counsel costs, travel waiting costs and waiting costs
   */
  public BigDecimal additionalCosts() {
    return netCounselCostsAmount.add(travelWaitingCostsAmount).add(netWaitingCostsAmount);
  }
}
