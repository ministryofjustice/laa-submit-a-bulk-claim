package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.util.Map;

/**
 * Holds the details of the matter starts for a bulk submission.
 *
 * @param matterTypes the map of matter starts for the bulk submission
 * @author Jamie Briggs
 */
public record SubmissionMatterStartsDetails(Map<SubmissionMatterStartsRow, Long> matterTypes) {}
