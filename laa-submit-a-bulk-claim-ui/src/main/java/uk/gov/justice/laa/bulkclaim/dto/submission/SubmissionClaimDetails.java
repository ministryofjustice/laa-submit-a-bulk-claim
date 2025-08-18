package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.util.List;

public record SubmissionClaimDetails(
    SubmissionCostsSummary costsSummary, List<SubmissionClaimRow> submissionClaims) {}
