package uk.gov.justice.laa.payments.submit.dto.submission.claim;

import java.math.BigDecimal;

public record SubmissionClaimRowCostsDetails(
    BigDecimal netProfitCostsAmount,
    BigDecimal netDisbursementAmount,
    BigDecimal disbursementsVatAmount,
    BigDecimal netCounselCostsAmount,
    BigDecimal travelWaitingCostsAmount,
    BigDecimal netWaitingCostsAmount,
    BigDecimal claimValue) {}
