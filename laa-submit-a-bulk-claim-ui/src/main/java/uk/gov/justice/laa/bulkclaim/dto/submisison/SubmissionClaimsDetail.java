package uk.gov.justice.laa.bulkclaim.dto.submisison;

import java.util.List;

public record SubmissionClaimsDetail(
    SubmissionCostsSummary costsSummary, List<SubmissionClaimRow> submissionClaims) {}
