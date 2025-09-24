package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimMessagesSummary;

/**
 * Summary of submissions and claim errors. Used in the submission summary page between two tables.
 *
 * @param submissions the submissions part of the bulk claim
 * @param claimMessagesSummary the claim errors part of the bulk claim
 */
@Deprecated(since = "Will be removed when BulkSubmissionImportedController is removed", forRemoval = true)
public record BulkClaimImportSummary(
    List<SubmissionSummaryRow> submissions, ClaimMessagesSummary claimMessagesSummary) {}
