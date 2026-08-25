package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels;

import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

public record ClaimStatusBanner(
    DerivedClaimStatus status, String lastEditedDate, String lastEditedTime) {

  public String label() {
    String name = status.name();
    return name.charAt(0) + name.substring(1).toLowerCase();
  }

  public boolean error() {
    return status == DerivedClaimStatus.VOIDED;
  }
}
