package uk.gov.justice.laa.bulkclaim.dto;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO to hold claims search form values.
 *
 * @param submissionId submission and related claims reference
 * @param submittedDateFrom submitted date range from
 * @param submittedDateTo submitted date range to
 */
public record SubmissionsSearchForm(
    String submissionId,
    @DateTimeFormat(pattern = "dd/MM/yyyy") String submittedDateFrom,
    @DateTimeFormat(pattern = "dd/MM/yyyy") String submittedDateTo) {}
