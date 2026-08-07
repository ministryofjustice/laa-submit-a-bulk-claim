package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

/** A {@link ClaimFieldRow} paired with its resolved display label, for the Values/Total tables. */
public record ClaimValueRow(String label, ClaimFieldRow row) {}
