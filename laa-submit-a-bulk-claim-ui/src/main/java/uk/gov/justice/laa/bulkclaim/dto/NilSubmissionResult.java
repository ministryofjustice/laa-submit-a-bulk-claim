package uk.gov.justice.laa.bulkclaim.dto;

import java.util.List;
import java.util.UUID;

public record NilSubmissionResult(UUID submissionId, List<String> errorMessages) {}
