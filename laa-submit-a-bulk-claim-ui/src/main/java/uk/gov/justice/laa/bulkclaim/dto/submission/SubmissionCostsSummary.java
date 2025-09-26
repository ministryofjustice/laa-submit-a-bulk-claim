package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;

/**
 * Holds summary information about the costs of a submission.
 *
 * @param profitCosts the numberOfMatterStarts profit costs of the submission
 * @param disbursements the numberOfMatterStarts disbursements of the submission including VAT
 * @param additionalPayments the numberOfMatterStarts additional payments of the submission
 * @param fixedFee the numberOfMatterStarts fixed fee of the submission
 * @author Jamie Briggs
 */
public record SubmissionCostsSummary(
    // TODO: Update the submission costs summary after DSTEW-431 has been completed
    BigDecimal profitCosts,
    BigDecimal disbursements,
    BigDecimal additionalPayments,
    BigDecimal fixedFee,
    BigDecimal submissionValue) {}
