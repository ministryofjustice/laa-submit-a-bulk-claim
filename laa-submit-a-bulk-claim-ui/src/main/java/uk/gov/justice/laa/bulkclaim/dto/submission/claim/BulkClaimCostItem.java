package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;

/**
 * Bulk claim cost item.
 *
 * @param enteredValue the entered value
 * @param calculatedValue the calculated value
 */
public record BulkClaimCostItem(BigDecimal enteredValue, BigDecimal calculatedValue) {}
