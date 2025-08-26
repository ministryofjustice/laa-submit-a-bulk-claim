package uk.gov.justice.laa.bulkclaim.response;

import java.time.LocalDate;

/** Claims Submission DTO for search response. */
public record SubmissionDto(
    String submissionId,
    String bulkSubmissionId,
    String officeAccountNumber,
    String submissionPeriod,
    String areaOfLaw,
    String status,
    String schedularNumber,
    String previousSubmissionId,
    String isNilSubmission,
    LocalDate submitted) {}
