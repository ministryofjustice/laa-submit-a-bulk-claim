package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;

public record BulkClaimCostItem(BigDecimal enteredValue, BigDecimal calculatedValue) {}
