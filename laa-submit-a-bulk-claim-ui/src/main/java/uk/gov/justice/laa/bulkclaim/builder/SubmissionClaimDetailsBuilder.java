package uk.gov.justice.laa.bulkclaim.builder;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionCostsSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimRowMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;

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
  private final SubmissionClaimRowMapper submissionClaimRowMapper;

  /**
   * Builds a {@link SubmissionClaimsDetails} object. This object contains a summary of the costs
   * constructed using the claims attached to the submission.
   *
   * @param submissionResponse The source submission response.
   * @return The built {@link SubmissionClaimsDetails} object.
   */
  public SubmissionClaimsDetails build(SubmissionResponse submissionResponse) {

    // Get all claims from data claims service
    List<SubmissionClaimRow> submissionClaimRows =
        submissionResponse.getClaims().stream()
            .map(
                x -> {
                  ClaimResponse submissionClaim =
                      dataClaimsRestClient
                          .getSubmissionClaim(submissionResponse.getSubmissionId(), x.getClaimId())
                          .block();
                  Integer totalElements =
                      dataClaimsRestClient
                          .getValidationMessages(
                              submissionResponse.getSubmissionId(),
                              x.getClaimId(),
                              ValidationMessageType.WARNING.getValue(),
                              null,
                              0)
                          .block()
                          .getTotalElements();
                  return Mono.zip(Mono.just(submissionClaim), Mono.just(totalElements));
                })
            .map(
                x ->
                    submissionClaimRowMapper.toSubmissionClaimRow(
                        x.block().getT1(), x.block().getT2()))
            .toList();

    BigDecimal totalProfitCosts =
        submissionClaimRows.stream()
            .map(x -> x.costsDetails().netProfitCostsAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal disbursements =
        submissionClaimRows.stream()
            .map(x -> x.costsDetails().netDisbursementAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal additionalPayments =
        submissionClaimRows.stream()
            .map(x -> x.costsDetails().additionalCosts())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal submissionValue =
        submissionClaimRows.stream()
            .map(x -> x.costsDetails().claimValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Add to the cost summary using claims
    SubmissionCostsSummary costSummary =
        new SubmissionCostsSummary(
            totalProfitCosts,
            disbursements,
            additionalPayments,
            // TODO: Where is fixed fee from?
            BigDecimal.ZERO,
            submissionValue);
    return new SubmissionClaimsDetails(costSummary, submissionClaimRows);
  }
}
