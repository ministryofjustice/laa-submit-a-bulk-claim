package uk.gov.justice.laa.bulkclaim.dto.submisison;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Holds summary information about a submission created by a bulk submission.
 *
 * @param submissionReference unique identifier for the submission
 * @param status the status of the submission
 * @param submissionPeriod submission period
 * @param officeAccount account number for the office
 * @param submissionValue total value of the submission
 * @param areaOfLaw type of submission
 * @param submitted date the submission was submitted
 * @author Jamie Briggs
 */
public record SubmissionSummary(
    UUID submissionReference,
    String status,
    String submissionPeriod,
    String officeAccount,
    BigDecimal submissionValue,
    String areaOfLaw,
    LocalDate submitted) {}
