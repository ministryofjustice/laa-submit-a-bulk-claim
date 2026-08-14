package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimStatusBanner;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

public record ClaimDetailPageData(
    AreaOfLaw areaOfLaw,
    boolean showCurrentCalculated,
    ClaimDetailView claimDetailView,
    ClaimStatusBanner banner) {}
