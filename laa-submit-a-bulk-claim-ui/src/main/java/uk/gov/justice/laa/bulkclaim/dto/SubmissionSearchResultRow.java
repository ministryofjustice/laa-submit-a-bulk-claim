package uk.gov.justice.laa.bulkclaim.dto;

import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;

/**
 * Represents a row in submission search results.
 *
 * @param submission the submission data
 * @param submissionPeriod the submission period display value
 * @param submissionPeriodSortValue the sortable submission period value
 */
public record SubmissionSearchResultRow(
    SubmissionBase submission, String submissionPeriod, Integer submissionPeriodSortValue) {}
