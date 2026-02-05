package uk.gov.justice.laa.bulkclaim.dto;

import lombok.Builder;

/**
 * DTO to hold claims search form values.
 *
 * @param submissionId submission and related claims reference
 * @param submissionPeriod the submission period
 */
@Builder
public record SubmissionsSearchForm(String submissionId, String submissionPeriod) {}
