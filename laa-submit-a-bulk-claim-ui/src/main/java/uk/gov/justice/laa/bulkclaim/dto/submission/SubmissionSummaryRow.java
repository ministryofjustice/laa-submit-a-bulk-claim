package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubmissionSummaryRow(
    OffsetDateTime submitted,
    UUID submissionReference,
    String officeAccount,
    String areaOfLaw,
    LocalDate submissionPeriod,
    int totalClaims) {}
