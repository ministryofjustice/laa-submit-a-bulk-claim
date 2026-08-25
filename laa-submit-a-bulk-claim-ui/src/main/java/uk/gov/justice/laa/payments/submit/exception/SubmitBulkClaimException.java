package uk.gov.justice.laa.payments.submit.exception;

public class SubmitBulkClaimException extends RuntimeException {

  public SubmitBulkClaimException(String message) {
    super(message);
  }

  public SubmitBulkClaimException(String message, Throwable cause) {
    super(message, cause);
  }
}
