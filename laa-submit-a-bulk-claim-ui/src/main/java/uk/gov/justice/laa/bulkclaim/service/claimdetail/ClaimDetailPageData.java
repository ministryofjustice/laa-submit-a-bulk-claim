package uk.gov.justice.laa.bulkclaim.service.claimdetail;

import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimStatusBanner;

public record ClaimDetailPageData(
    String ufn,
    boolean showCurrentCalculated,
    ClaimDetailView claimDetailView,
    ClaimStatusBanner banner) {}
