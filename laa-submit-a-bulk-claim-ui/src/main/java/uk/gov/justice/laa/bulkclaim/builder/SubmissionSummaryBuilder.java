package uk.gov.justice.laa.bulkclaim.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.ClaimValidationError;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Builder class for constructing a {@link BulkClaimSummary} object used for displaying submission
 * summary information to the user.
 *
 * @author Jamie Briggs
 */
@Component
@RequiredArgsConstructor
public class SubmissionSummaryBuilder {

  private final DataClaimsRestService dataClaimsRestService;
  private final BulkClaimSummaryMapper bulkClaimSummaryMapper;

  /**
   * Builds a {@link BulkClaimSummary} using a {@link GetSubmission200Response}.
   *
   * @param submissionResponse The source submission response..
   * @return The built {@link BulkClaimSummary}.
   */
  public BulkClaimSummary build(List<GetSubmission200Response> submissionResponse) {

    // Get all summary rows
    List<SubmissionSummaryRow> summaryRows =
        bulkClaimSummaryMapper.toSubmissionSummaryRows(submissionResponse);

    // Get all errors using the data claims rest service.
    Map<UUID, List<ClaimValidationError>> errorMap =
        summaryRows.stream()
            .collect(
                Collectors.toMap(
                    SubmissionSummaryRow::submissionReference, // Key: submissionReference
                    row ->
                        dataClaimsRestService
                          .getValidationErrors(row.submissionReference()) // Value: List of errors
                            .blockOptional()
                            .orElse(List.of()) // Handle empty results
                    ));

    // Loop through an error map and add claims
    List<SubmissionSummaryClaimError> errorList = new ArrayList<>();
    errorMap
        .keySet()
        .forEach(
            submissionReference ->
                errorList.addAll(
                    errorMap.get(submissionReference).stream()
            .map(x -> bulkClaimSummaryMapper.toSubmissionSummaryClaimError(submissionReference, x))
            .toList()));

    return new BulkClaimSummary(summaryRows, errorList);
  }
}
