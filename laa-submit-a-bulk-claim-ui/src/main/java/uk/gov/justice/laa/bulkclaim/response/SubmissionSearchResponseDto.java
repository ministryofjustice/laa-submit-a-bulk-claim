package uk.gov.justice.laa.bulkclaim.response;

import java.util.List;

/** Submissions search response containing related claims. */
public record SubmissionSearchResponseDto(List<SubmissionDto> submissions) {}
