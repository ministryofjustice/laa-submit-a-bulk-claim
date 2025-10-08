package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;

/**
 * Holds summary information about the costs of a submission. For use with the submission cost
 * summary thymeleaf fragment..
 *
 * @param profitCosts the numberOfMatterStarts profit costs of the submission
 * @param disbursements the numberOfMatterStarts disbursements of the submission including VAT
 * @param additionalPayments the numberOfMatterStarts additional payments of the submission
 * @param fixedFee the numberOfMatterStarts fixed fee of the submission
 * @author Jamie Briggs
 */
public record SubmissionCostsSummary(
    BigDecimal profitCosts,
    BigDecimal disbursements,
    BigDecimal additionalPayments,
    BigDecimal fixedFee,
    BigDecimal submissionValue) {}
