package uk.gov.justice.laa.bulkclaim.builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimRowMapper;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
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
  private final PaginationUtil paginationUtil;

  /**
   * Builds a {@link SubmissionClaimsDetails} object. This object contains a summary of the costs
   * constructed using the claims attached to the submission.
   *
   * @param submissionResponse The source submission response.
   * @return The built {@link SubmissionClaimsDetails} object.
   */
  public SubmissionClaimsDetails build(SubmissionResponse submissionResponse, int page, int size) {

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
                              0,
                              size)
                          .block()
                          .getTotalElements();
                  return Mono.zip(Mono.just(submissionClaim), Mono.just(totalElements));
                })
            .map(
                x ->
                    submissionClaimRowMapper.toSubmissionClaimRow(
                        x.block().getT1(), x.block().getT2()))
            .toList();

    int totalClaims = submissionClaimRows.size();
    BigDecimal totalClaimValue =
        submissionClaimRows.stream()
            .map(SubmissionClaimRow::costsDetails)
            .filter(Objects::nonNull)
            .map(SubmissionClaimRowCostsDetails::claimValue)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    int safeSize = size > 0 ? size : totalClaims;
    int maxPageIndex =
        safeSize == 0 ? 0 : Math.max((int) Math.ceil((double) totalClaims / safeSize) - 1, 0);
    int safePage = Math.clamp(page, 0, maxPageIndex);
    int fromIndex = Math.min(safePage * safeSize, totalClaims);
    int toIndex = Math.min(fromIndex + safeSize, totalClaims);
    List<SubmissionClaimRow> pagedClaims = submissionClaimRows.subList(fromIndex, toIndex);

    return new SubmissionClaimsDetails(
        pagedClaims, paginationUtil.from(safePage, safeSize, totalClaims), totalClaimValue);
  }
}
