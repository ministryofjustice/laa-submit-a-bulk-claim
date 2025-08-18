package uk.gov.justice.laa.bulkclaim.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionFields;

@Component
@RequiredArgsConstructor
public class SubmissionSummaryBuilder {

  private final DataClaimsRestService dataClaimsRestService;

  public SubmissionSummary build(GetSubmission200Response submissionResponse) {

    SubmissionFields submission = submissionResponse.getSubmission();
    return new SubmissionSummary(
        submission.getSubmissionId(),
        // TODO: Add submission status when available on OpenAPI spec.
        "Submitted",
        submission.getSubmissionPeriod(),
        submission.getOfficeAccountNumber(),
        // TODO: Add submission value when available on OpenAPI spec.
        BigDecimal.valueOf(1000.05f),
        submission.getAreaOfLaw(),
        // TODO: Add submission submitted date when available on OpenAPI spec.
        LocalDate.of(2020, 1, 1));
  }
}
