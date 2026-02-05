package uk.gov.justice.laa.bulkclaim.dto;

import java.util.List;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

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
    String submissionPeriod,
    AreaOfLaw areaOfLaw,
    List<String> offices,
    SubmissionStatus submissionStatus) {}
