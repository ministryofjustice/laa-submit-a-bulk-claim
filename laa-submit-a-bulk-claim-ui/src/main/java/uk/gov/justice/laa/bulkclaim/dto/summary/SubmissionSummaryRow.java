package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Summary of a single submission.
 *
 * @param submissionReference unique identifier for the submission
 * @param officeAccount account number for the office
 * @param areaOfLaw type of submission
 * @param submissionPeriod submission period
 * @param totalClaims total number of claims in the submission
 * @author Jamie Briggs
 */
public record SubmissionSummaryRow(
    UUID submissionReference,
    String officeAccount,
    String areaOfLaw,
    LocalDate submissionPeriod,
    int totalClaims) {}
