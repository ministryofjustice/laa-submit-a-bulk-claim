package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import java.util.List;
import java.util.Optional;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

/**
 * Summary of claim errors.
 *
 * @param messages list of claim messages rows
 * @param totalMessageCount calculatedTotal number of errors found
 * @param totalClaimsWithErrors calculatedTotal number of unique claims with errors
 */
@Builder
public record MessagesSummary(
    List<MessageRow> messages,
    int totalMessageCount,
    int totalClaimsWithErrors,
    Page pagination,
    MessagesSource messagesSource) {

  /**
   * Returns true if there are any errors in the bulk claim.
   *
   * @return true if calculatedTotal error numberOfMatterStarts is greater than zero
   */
  public boolean containsErrors() {
    return totalErrors() > 0;
  }

  /**
   * Returns the calculatedTotal number of messages found in the bulk claim.
   *
   * @return the calculatedTotal error numberOfMatterStarts
   */
  public long totalErrors() {
    return Optional.ofNullable(messages).stream()
        .flatMap(List::stream)
        .filter(x -> "error".equalsIgnoreCase(x.type()))
        .count();
  }

  /**
   * Returns the number of claims that have one or more errors.
   *
   * @return the calculatedTotal number of unique claims with errors
   */
  public int totalClaimsWithErrors() {
    return totalClaimsWithErrors;
  }
}
