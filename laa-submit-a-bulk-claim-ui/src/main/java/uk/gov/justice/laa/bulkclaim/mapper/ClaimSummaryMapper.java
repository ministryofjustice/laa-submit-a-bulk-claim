package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Maps between {@link ClaimResponse} and {@link ClaimSummary}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface ClaimSummaryMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(
      target = "isEscaped",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.escapeCaseFlag")
  @Mapping(target = "uniqueClientNumber2", source = "claimResponse.client2Ucn")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw")
  @Mapping(target = "officeAccountNumber", source = "submissionResponse.officeAccountNumber")
  @Mapping(target = "standardFeeCategoryCode", source = "claimResponse.standardFeeCategoryCode")
  @Mapping(target = "matterType1", source = "claimResponse.matterTypeCode", qualifiedByName = "matterType1")
  @Mapping(target = "matterType2", source = "claimResponse.matterTypeCode", qualifiedByName = "matterType2")
  ClaimSummary toClaimSummary(ClaimResponse claimResponse, SubmissionResponse submissionResponse, String areaOfLaw);

  @Named("matterType1")
  default String getMatterType1(String matterTypeCode) {
      if (matterTypeCode == null) return null;
      return matterTypeCode.split(":")[0];
  }
    @Named("matterType2")
    default String getMatterType2(String matterTypeCode) {
        if (matterTypeCode == null || !matterTypeCode.contains(":")) return null;
        return matterTypeCode.split(":")[1];
    }
}
