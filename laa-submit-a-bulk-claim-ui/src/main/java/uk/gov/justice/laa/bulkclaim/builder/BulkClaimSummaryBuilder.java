package uk.gov.justice.laa.bulkclaim.builder;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

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
    //    Map<UUID, List<ClaimValidationError>> errorMap =
    //        summaryRows.stream()
    //            .collect(
    //                Collectors.toMap(
    //                    SubmissionSummaryRow::submissionReference, // Key: submissionReference
    //                    row ->
    //                        dataClaimsRestService
    //                            .getValidationErrors(row.submissionReference()) // Value: List of
    // errors
    //                            .blockOptional()
    //                            .orElse(List.of()) // Handle empty results
    //                    ));

    // Loop through an error map and add claims
    List<SubmissionSummaryClaimErrorRow> errorList = new ArrayList<>();
    //    errorMap
    //        .keySet()
    //        .forEach(
    //            submissionReference ->
    //                errorList.addAll(
    //                    errorMap.get(submissionReference).stream()
    //                        .map(
    //                            x ->
    //                                bulkClaimImportSummaryMapper.toSubmissionSummaryClaimError(
    //                                    submissionReference, x))
    //                        .toList()));

    return new BulkClaimImportSummary(summaryRows, errorList);
  }
}
