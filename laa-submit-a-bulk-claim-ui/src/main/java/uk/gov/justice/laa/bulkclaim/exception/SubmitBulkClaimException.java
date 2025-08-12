package uk.gov.justice.laa.bulkclaim.exception;

/**
 * Generic runtime exception for submit a bulk claim service.
 *
 * @author Jamie Briggs
 */
public class SubmitBulkClaimException extends RuntimeException {

  public SubmitBulkClaimException(String message) {
    super(message);
  }
}
