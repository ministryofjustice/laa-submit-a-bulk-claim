package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;

public record SubmissionCostsSummary(
    BigDecimal profitCosts,
    BigDecimal disbursements,
    BigDecimal additionalPayments,
    BigDecimal fixedFee,
    BigDecimal submissionValue) {}
