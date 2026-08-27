package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import static uk.gov.justice.laa.payments.submit.util.MatterTypeUtil.FIRST_PART;
import static uk.gov.justice.laa.payments.submit.util.MatterTypeUtil.SECOND_PART;

import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.payments.submit.util.MatterTypeUtil;

@Getter
public enum LegalHelpClaimDetailsViewField implements ClaimViewField<LegalHelpClaimDetails> {

  // Page header / Summary
  CATEGORY_OF_LAW(LegalHelpClaimDetails::getCategoryOfLaw),
  MATTER_TYPE_1(
      LegalHelpClaimDetails::getMatterTypeCodeOne, MatterTypeUtil.partIdentifier(FIRST_PART)),
  MATTER_TYPE_2(
      LegalHelpClaimDetails::getMatterTypeCodeTwo, MatterTypeUtil.partIdentifier(SECOND_PART)),
  // London rate not part of calculation - shown in the Values table with no calculated/assessed
  // column, so it is wrapped as a ClaimFieldRow to match that table's row shape.
  LONDON_RATE(LegalHelpClaimDetailsViewField::londonRateRow, "claimSummaryFee.isLondonRate"),

  // Values
  COUNSELS_COSTS(LegalHelpClaimDetails::getCounselsCosts, "claimSummaryFee.netCounselCostsAmount"),
  TRAVEL_AND_WAITING_COSTS(
      LegalHelpClaimDetails::getTravelAndWaitingCosts, "claimSummaryFee.travelWaitingCostsAmount"),
  DETENTION_TRAVEL_WAITING_COSTS(
      LegalHelpClaimDetails::getDetentionTravelWaitingCosts,
      "claimSummaryFee.detentionTravelWaitingCostsAmount"),
  JR_FORM_FILLING(LegalHelpClaimDetails::getJrFormFilling, "claimSummaryFee.jrFormFillingAmount"),
  ADJOURNED_HEARING_FEE(
      LegalHelpClaimDetails::getAdjournedHearingFee, "claimSummaryFee.adjournedHearingFeeAmount"),
  CMRH_ORAL(LegalHelpClaimDetails::getCmrhOral, "claimSummaryFee.cmrhOralCount"),
  CMRH_TELEPHONE(LegalHelpClaimDetails::getCmrhTelephone, "claimSummaryFee.cmrhTelephoneCount"),
  HOME_OFFICE_INTERVIEW(
      LegalHelpClaimDetails::getHomeOfficeInterview, "claimSummaryFee.hoInterview"),
  SUBSTANTIVE_HEARING(
      LegalHelpClaimDetails::getSubstantiveHearing, "claimSummaryFee.isSubstantiveHearing");

  private static ClaimFieldRow londonRateRow(LegalHelpClaimDetails claim) {
    return new ClaimFieldRow(claim.getReportedLondonRateIndicator(), null, null);
  }

  private final Function<LegalHelpClaimDetails, Object> accessor;
  private final Set<String> claimsApiFieldNames;

  LegalHelpClaimDetailsViewField(
      Function<LegalHelpClaimDetails, Object> accessor, String... claimsApiFieldNames) {
    this.accessor = accessor;
    this.claimsApiFieldNames = Set.of(claimsApiFieldNames);
  }
}
