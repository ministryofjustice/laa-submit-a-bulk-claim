package uk.gov.justice.laa.bulkclaim.dto;

/**
 * DTO to hold claims search form values.
 *
 * @param submissionId submission and related claims reference
 * @param submittedDateFrom submitted date range from
 * @param submittedDateTo submitted date range to
 */
public record SubmissionsSearchForm(
    String submissionId, String submittedDateFrom, String submittedDateTo) {}
