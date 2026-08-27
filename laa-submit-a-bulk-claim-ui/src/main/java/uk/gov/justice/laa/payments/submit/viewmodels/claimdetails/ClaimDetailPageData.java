package uk.gov.justice.laa.payments.submit.viewmodels.claimdetails;

import java.util.Set;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimStatusBanner;

public record ClaimDetailPageData(
    AreaOfLaw areaOfLaw,
    boolean showCurrentCalculated,
    ClaimDetailView claimDetailView,
    ClaimStatusBanner banner,
    Set<String> amendedFields) {}
