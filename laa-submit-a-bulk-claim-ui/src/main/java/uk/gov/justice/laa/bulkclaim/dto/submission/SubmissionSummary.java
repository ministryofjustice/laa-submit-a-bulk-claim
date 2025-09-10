package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Holds summary information about a submission created by a bulk submission.
 *
 * @param submissionReference unique identifier for the submission
 * @param status the status of the submission
 * @param submissionPeriod submission period
 * @param officeAccount account number for the office
 * @param submissionValue numberOfMatterStarts value of the submission
 * @param areaOfLaw type of submission
 * @param submitted date and time the submission was submitted
 * @author Jamie Briggs
 */
public record SubmissionSummary(
    UUID submissionReference,
    String status,
    LocalDate submissionPeriod,
    String officeAccount,
    BigDecimal submissionValue,
    String areaOfLaw,
    OffsetDateTime submitted) {}
