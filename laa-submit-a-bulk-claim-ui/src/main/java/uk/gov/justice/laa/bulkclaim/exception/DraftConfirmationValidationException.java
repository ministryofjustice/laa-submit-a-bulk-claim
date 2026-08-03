package uk.gov.justice.laa.bulkclaim.exception;

import java.util.List;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.confirmation.ClaimConfirmationError;

/** Indicates that claim-level validation errors prevented draft confirmation. */
@Getter
public class DraftConfirmationValidationException extends RuntimeException {

  private final List<ClaimConfirmationError> claimReports;

  public DraftConfirmationValidationException(List<ClaimConfirmationError> claimReports) {
    super("Submission cannot be confirmed");
    this.claimReports = List.copyOf(claimReports);
  }
}
