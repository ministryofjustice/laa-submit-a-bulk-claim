package uk.gov.justice.laa.bulkclaim.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record SubmissionsSearchForm(
    String submissionPeriod,
    String areaOfLaw,
    List<String> offices,
    SubmissionOutcomeFilter submissionStatuses) {}
