package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import java.util.List;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

@Builder
public record MessagesSummary(
    List<MessageRow> messages,
    int totalMessageCount,
    int totalClaimsWithErrors,
    Page pagination,
    MessagesSource messagesSource) {

  public int totalClaimsWithErrors() {
    return totalClaimsWithErrors;
  }
}
