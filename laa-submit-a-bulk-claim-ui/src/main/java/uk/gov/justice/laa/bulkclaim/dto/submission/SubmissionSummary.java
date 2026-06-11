package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubmissionSummary(
    UUID submissionReference,
    String status,
    LocalDate submissionPeriod,
    String officeAccount,
    BigDecimal submissionValue,
    String areaOfLaw,
    OffsetDateTime submitted) {}
