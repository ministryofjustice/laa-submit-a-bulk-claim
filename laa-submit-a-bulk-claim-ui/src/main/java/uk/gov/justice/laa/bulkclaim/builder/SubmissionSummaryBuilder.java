package uk.gov.justice.laa.bulkclaim.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submisison.SubmissionSummary;
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
        "Submitted",
        submission.getSubmissionPeriod(),
        submission.getOfficeAccountNumber(),
        BigDecimal.valueOf(1000.05f),
        submission.getAreaOfLaw(),
        LocalDate.of(2020, 1, 1));
  }
}
