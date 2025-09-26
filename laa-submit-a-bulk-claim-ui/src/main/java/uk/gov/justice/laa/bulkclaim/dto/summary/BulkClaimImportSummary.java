package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.List;

/**
 * Summary of submissions and claim errors. Used in the submission summary page between two tables.
 *
 * @param submissions the submissions part of the bulk claim
 * @param claimMessagesSummary the claim errors part of the bulk claim
 */
public record BulkClaimImportSummary(
    List<SubmissionSummaryRow> submissions, ClaimMessagesSummary claimMessagesSummary) {}
