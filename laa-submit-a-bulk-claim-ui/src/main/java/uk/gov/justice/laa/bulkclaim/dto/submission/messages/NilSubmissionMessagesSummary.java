package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Builder
public record NilSubmissionMessagesSummary(
    List<String> messages,
    int totalMessageCount,
    String submissionReference,
    String submissionPeriod,
    String officeAccount,
    AreaOfLaw areaOfLaw,
    OffsetDateTime submitted) {}
