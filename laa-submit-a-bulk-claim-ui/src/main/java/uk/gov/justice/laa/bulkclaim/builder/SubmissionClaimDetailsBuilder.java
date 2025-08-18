package uk.gov.justice.laa.bulkclaim.builder;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionCostsSummary;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

@Component
@RequiredArgsConstructor
public class SubmissionClaimDetailsBuilder {

  private final DataClaimsRestService dataClaimsRestService;
  private final SubmissionClaimMapper submissionClaimMapper;

  public SubmissionClaimDetails build(GetSubmission200Response submissionResponse) {

    // Get all claims from data claims service
    List<SubmissionClaimRow> submissionClaimRows =
        submissionResponse.getClaims().stream()
            .map(
                x ->
                    dataClaimsRestService.getSubmissionClaim(
                        submissionResponse.getSubmission().getSubmissionId(), x.getClaimId()))
            .map(x -> submissionClaimMapper.toSubmissionClaimRow(x.block()))
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

    // Add to cost summary using claims
    SubmissionCostsSummary costSummary =
        new SubmissionCostsSummary(
            totalProfitCosts,
            disbursements,
            additionalPayments,
            // Where is fixed fee from?
            BigDecimal.ZERO);
    return new SubmissionClaimDetails(costSummary, submissionClaimRows);
  }
}
