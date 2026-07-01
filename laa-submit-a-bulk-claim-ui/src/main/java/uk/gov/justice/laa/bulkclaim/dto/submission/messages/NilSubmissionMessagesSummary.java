package uk.gov.justice.laa.bulkclaim.dto.submission.messages;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record NilSubmissionMessagesSummary(
        List<String> messages,
        int totalMessageCount,
        String submissionReference,
        String submissionPeriod,
        String officeAccount,
        String areaOfLaw,
        OffsetDateTime submitted) {
}
