package uk.gov.justice.laa.bulkclaim.dto;

import java.util.List;
import lombok.Builder;

/**
 * DTO to hold claims search form values.
 *
 * @param submissionPeriod the submission period
 * @param areaOfLaw the submission areaOfLaw
 * @param offices the submission offices
 * @param submissionStatus the submission status
 */
@Builder
public record SubmissionsSearchForm(
    String submissionPeriod, String areaOfLaw, List<String> offices, String submissionStatus) {}
