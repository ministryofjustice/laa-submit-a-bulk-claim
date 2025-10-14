package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import java.util.List;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

/**
 * Holds claim information about a submission created by a bulk submission, including a summary of
 * the costs, and a list of claims.
 *
 * @param submissionClaims the claims attached to the submission
 * @param totalClaimValue aggregated total of all claim values in the submission
 */
public record SubmissionClaimsDetails(
    List<SubmissionClaimRow> submissionClaims, Page pagination, BigDecimal totalClaimValue) {}
