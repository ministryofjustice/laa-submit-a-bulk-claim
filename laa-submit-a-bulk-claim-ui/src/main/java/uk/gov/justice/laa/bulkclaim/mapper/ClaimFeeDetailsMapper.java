package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimFeeCalculationDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

/**
 * Maps between {@link ClaimResponse} and {@link SubmissionClaimDetails}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface ClaimFeeDetailsMapper {

  // TODO: Add new calculated fee details to the claim response object and map. This method
  //  technically maps the submitted details.
  SubmissionClaimFeeCalculationDetails toSubmittedFeeDetails(ClaimResponse claimFields);
}
