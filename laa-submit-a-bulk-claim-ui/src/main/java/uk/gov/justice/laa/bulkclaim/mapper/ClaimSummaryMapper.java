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

  /**
   * Extracts the first part of the matter type code (matterType1).
   *
   * @param matterTypeCode the matterTpeCode string
   * @return the first part of the matterTypeCode, or null if input is null
   */
  @Named("matterType1")
  default String getMatterType1(String matterTypeCode) {
    if (matterTypeCode == null) {
      return null;
    }
    return matterTypeCode.split(":")[0];
  }

  /**
   * Extracts the second part of the matter type code (matterType2) if it exists.
   *
   * @param matterTypeCode the matterTpeCode string
   * @return the second part of the matterTypeCode, or null if input is null
   */
  @Named("matterType2")
  default String getMatterType2(String matterTypeCode) {
    if (matterTypeCode == null || !matterTypeCode.contains(":")) {
      return null;
    }
    return matterTypeCode.split(":")[1];
  }

  /**
   * Builds the client's full name from forename and surname.
   *
   * @param claimResponse the claimResponse containing client details
   * @return the combined client name, or an empty string if both values are null
   */
  @Named("mapClientName")
  default String getClientName(ClaimResponse claimResponse) {
    return Stream.of(claimResponse.getClientForename(), claimResponse.getClientSurname())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }

  /**
   * Builds the second client's full name from forename and surname.
   *
   * @param claimResponse the claimResponse containing client details
   * @return the combined client2 name, or an empty string if both values are null
   */
  @Named("mapClient2Name")
  default String getClient2Name(ClaimResponse claimResponse) {
    return Stream.of(claimResponse.getClient2Forename(), claimResponse.getClient2Surname())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }
}
