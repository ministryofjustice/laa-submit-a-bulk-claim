package uk.gov.justice.laa.payments.submit.dto;

import java.util.List;
import java.util.UUID;

public record NilSubmissionResult(UUID submissionId, List<String> errorMessages) {}
