package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.util.List;

/**
 * Holds the details of the matter starts for a bulk submission.
 *
 * @param matterTypes the list of matter starts for the bulk submission
 * @author Jamie Briggs
 */
public record SubmissionMatterStartsDetails(List<SubmissionMatterStartsRow> matterTypes) {}
