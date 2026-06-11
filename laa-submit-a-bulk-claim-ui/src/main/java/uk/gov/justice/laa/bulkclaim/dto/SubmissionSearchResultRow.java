package uk.gov.justice.laa.bulkclaim.dto;

import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;

public record SubmissionSearchResultRow(
    SubmissionBase submission, String submissionPeriod, Integer submissionPeriodSortValue) {}
