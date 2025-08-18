package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;

/**
 * Holds summary information about the costs of a submission.
 *
 * @param profitCosts the total profit costs of the submission
 * @param disbursements the total disbursements of the submission including VAT
 * @param additionalPayments the total additional payments of the submission
 * @param fixedFee the total fixed fee of the submission
 * @author Jamie Briggs
 */
public record SubmissionCostsSummary(
    BigDecimal profitCosts,
    BigDecimal disbursements,
    BigDecimal additionalPayments,
    // TODO: Where is fixed fee from?
    BigDecimal fixedFee) {

  /**
   * Returns the total value of the submission.
   *
   * @return the total value of the submission
   */
  public BigDecimal submissionValue() {
    return profitCosts.add(disbursements).add(additionalPayments);
  }
}
