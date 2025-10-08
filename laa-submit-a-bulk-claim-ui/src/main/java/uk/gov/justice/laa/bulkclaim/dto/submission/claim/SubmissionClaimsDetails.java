package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.util.List;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

/**
 * Holds claim information about a submission created by a bulk submission, including a summary of
 * the costs, and a list of claims.
 *
 * @param submissionClaims the claims attached to the submission
 * @author Jamie Briggs
 */
public record SubmissionClaimsDetails(List<SubmissionClaimRow> submissionClaims, Page pagination) {}
