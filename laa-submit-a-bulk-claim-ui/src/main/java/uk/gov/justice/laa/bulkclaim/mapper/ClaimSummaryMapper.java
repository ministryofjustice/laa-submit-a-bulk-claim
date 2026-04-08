package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

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
  @Mapping(target = "officeAccountNumber", source = "officeAccountNumber")
  @Mapping(target = "standardFeeCategoryCode", source = "claimResponse.standardFeeCategoryCode")
  @Mapping(
      target = "matterType1",
      source = "claimResponse.matterTypeCode",
      qualifiedByName = "matterType1")
  @Mapping(
      target = "matterType2",
      source = "claimResponse.matterTypeCode",
      qualifiedByName = "matterType2")
  @Mapping(target = "categoryOfLaw", source = "claimResponse.feeCalculationResponse.categoryOfLaw")
  @Mapping(
      target = "feeCodeDescription",
      source = "claimResponse.feeCalculationResponse.feeCodeDescription")
  @Mapping(target = "submissionDate", source = "submissionDate")
  @Mapping(target = "clientName", source = "claimResponse", qualifiedByName = "mapClientName")
  @Mapping(target = "client2Name", source = "claimResponse", qualifiedByName = "mapClient2Name")
  ClaimSummary toClaimSummary(
      ClaimResponse claimResponse,
      String areaOfLaw,
      String officeAccountNumber,
      OffsetDateTime submissionDate);

  @Named("matterType1")
  default String getMatterType1(String matterTypeCode) {
    if (matterTypeCode == null) {
        return null;
    }
    return matterTypeCode.split(":")[0];
  }

  @Named("matterType2")
  default String getMatterType2(String matterTypeCode) {
    if (matterTypeCode == null || !matterTypeCode.contains(":")) {
        return null;
    }
    return matterTypeCode.split(":")[1];
  }

  @Named("mapClient2Name")
  default String getClient2Name(ClaimResponse claimResponse) {
    return Stream.of(claimResponse.getClient2Forename(), claimResponse.getClient2Surname())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }

  @Named("mapClientName")
  default String getClientName(ClaimResponse claimResponse) {
    return Stream.of(claimResponse.getClientForename(), claimResponse.getClientSurname())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }
}
