package uk.gov.justice.laa.payments.submit.dto.submission.claim;

import java.math.BigDecimal;

public record BulkClaimCostItem(BigDecimal enteredValue, BigDecimal calculatedValue) {}
