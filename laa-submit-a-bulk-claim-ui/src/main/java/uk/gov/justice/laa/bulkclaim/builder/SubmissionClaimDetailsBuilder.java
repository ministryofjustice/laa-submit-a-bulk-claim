package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimRowMapper;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSetV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Builder class for constructing a {@link SubmissionClaimsDetails} object used for displaying a
 * table of claim details to the user.
 *
 * @author Jamie Briggs
 */
@Component
@RequiredArgsConstructor
public class SubmissionClaimDetailsBuilder {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final DataClaimsRestClientV2 dataClaimsRestClientV2;
  private final SubmissionClaimRowMapper submissionClaimRowMapper;
  private final PaginationUtil paginationUtil;

  /**
   * Builds a {@link SubmissionClaimsDetails} object. This object contains a summary of the costs
   * constructed using the claims attached to the submission.
   *
   * @param submissionResponse The source submission response.
   * @return The built {@link SubmissionClaimsDetails} object.
   */
  public SubmissionClaimsDetails build(SubmissionResponse submissionResponse, int page, int size) {
    var submissionClaimData =
        dataClaimsRestClient
            .getClaims(
                submissionResponse.getOfficeAccountNumber(),
                submissionResponse.getSubmissionId(),
                page,
                size)
            .getBody();
    // Get all claims from data claims service
    List<SubmissionClaimRow> submissionClaimRows =
        submissionClaimData.getContent().stream()
            .map(x -> submissionClaimRowMapper.toSubmissionClaimRow(x, x.getTotalWarnings()))
            .toList();

    return new SubmissionClaimsDetails(
        submissionClaimRows,
        paginationUtil.from(
            submissionClaimData.getNumber(),
            submissionClaimData.getSize(),
            submissionClaimData.getTotalElements()),
        submissionResponse.getCalculatedTotalAmount());
  }

  /**
   * Builds a {@link SubmissionClaimsDetails} object. This object contains a summary of the costs
   * and a list of claims associated with a specific submission, retrieved with sorting and
   * pagination information.
   *
   * @param submissionResponse The submission response containing the office account number and
   *     submission ID for the claims.
   * @param page The zero-based page number for claim pagination.
   * @param size The number of claims to retrieve per page.
   * @param sort The sorting criteria for the retrieved claims.
   * @return A {@link SubmissionClaimsDetails} object containing the mapped claims, pagination
   *     details, and the calculated total amount for the submission.
   */
  public SubmissionClaimsDetails build(
      SubmissionResponse submissionResponse, int page, int size, String sort) {
    ClaimResultSetV2 claimResultSetV2 =
        dataClaimsRestClientV2
            .getClaims(
                submissionResponse.getOfficeAccountNumber(),
                submissionResponse.getSubmissionId(),
                page,
                size,
                sort)
            .getBody();

    List<SubmissionClaimRow> submissionClaimRows =
        claimResultSetV2.getContent().stream()
            .map(submissionClaimRowMapper::toSubmissionClaimRow)
            .toList();
    return new SubmissionClaimsDetails(
        submissionClaimRows,
        paginationUtil.from(
            claimResultSetV2.getNumber(),
            claimResultSetV2.getSize(),
            claimResultSetV2.getTotalElements()),
        submissionResponse.getCalculatedTotalAmount());
  }
}
