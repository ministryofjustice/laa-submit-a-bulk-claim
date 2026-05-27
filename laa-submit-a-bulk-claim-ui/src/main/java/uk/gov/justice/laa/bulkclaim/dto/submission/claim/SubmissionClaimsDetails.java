package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.math.BigDecimal;
import java.util.List;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

public record SubmissionClaimsDetails(
    List<SubmissionClaimRow> submissionClaims, Page pagination, BigDecimal totalClaimValue) {}
