package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Summary of a single submission.
 *
 * @param submitted date and time of when submission was made
 * @param submissionReference unique identifier for the submission
 * @param officeAccount account number for the office
 * @param areaOfLaw type of submission
 * @param submissionPeriod submission period
 * @param totalClaims numberOfMatterStarts number of claims in the submission
 * @author Jamie Briggs
 */
public record SubmissionSummaryRow(
    OffsetDateTime submitted,
    UUID submissionReference,
    String officeAccount,
    String areaOfLaw,
    LocalDate submissionPeriod,
    int totalClaims) {}
