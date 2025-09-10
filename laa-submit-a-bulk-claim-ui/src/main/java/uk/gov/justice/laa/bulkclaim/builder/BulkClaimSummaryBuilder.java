package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationErrorsResponse;

/**
 * Builder class for constructing a {@link BulkClaimImportSummary} object used for displaying
 * submission summary information to the user.
 *
 * @author Jamie Briggs
 */
@Component
@RequiredArgsConstructor
public class BulkClaimSummaryBuilder {

  private final DataClaimsRestService dataClaimsRestService;
  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  /**
   * Builds a {@link BulkClaimImportSummary} using a {@link SubmissionResponse}.
   *
   * @param submissionResponse The source submission response..
   * @return The built {@link BulkClaimImportSummary}.
   */
  public BulkClaimImportSummary build(List<SubmissionResponse> submissionResponse) {

    // Get all summary rows
    List<SubmissionSummaryRow> summaryRows =
        bulkClaimImportSummaryMapper.toSubmissionSummaryRows(submissionResponse);

    // Get all errors using the data claims rest service.
    // we only currently support one submission at a time so get the first one
    ValidationErrorsResponse errorResponse =
        dataClaimsRestService
            .getValidationErrors(summaryRows.getFirst().submissionReference())
            .block();

    // todo call the get claims endpoint for ufn,ucn and client name.

    // Loop through an error map and add claims
    List<SubmissionSummaryClaimErrorRow> errorList =
        Optional.ofNullable(errorResponse)
            .map(ValidationErrorsResponse::getContent)
            .orElseGet(List::of) // safe empty list
            .stream()
            .map(bulkClaimImportSummaryMapper::toSubmissionSummaryClaimError)
            .toList();

    int totalErrorCount =
        Optional.ofNullable(errorResponse)
            .map(ValidationErrorsResponse::getTotalElements)
            .orElse(0);

    int totalClaimsWithErrors =
        Optional.ofNullable(errorResponse).map(ValidationErrorsResponse::getTotalClaims).orElse(0);

    return new BulkClaimImportSummary(
        summaryRows, errorList, totalErrorCount, totalClaimsWithErrors);
  }
}
