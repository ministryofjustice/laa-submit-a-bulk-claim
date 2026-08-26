package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.LegalHelpClaimDetails;

@Getter
public enum LegalHelpClaimDetailsViewField implements ClaimViewField<LegalHelpClaimDetails> {

  // Page header / Summary
  CATEGORY_OF_LAW(LegalHelpClaimDetails::getCategoryOfLaw),
  MATTER_TYPE_1(LegalHelpClaimDetails::getMatterTypeCodeOne),
  MATTER_TYPE_2(LegalHelpClaimDetails::getMatterTypeCodeTwo),
  // London rate not part of calculation - shown in the Values table with no calculated/assessed
  // column, so it is wrapped as a ClaimFieldRow to match that table's row shape.
  LONDON_RATE(claim -> new ClaimFieldRow(claim.getReportedLondonRateIndicator(), null, null)),

  // Values
  COUNSELS_COSTS(LegalHelpClaimDetails::getCounselsCosts),
  TRAVEL_AND_WAITING_COSTS(LegalHelpClaimDetails::getTravelAndWaitingCosts),
  DETENTION_TRAVEL_WAITING_COSTS(LegalHelpClaimDetails::getDetentionTravelWaitingCosts),
  JR_FORM_FILLING(LegalHelpClaimDetails::getJrFormFilling),
  ADJOURNED_HEARING_FEE(LegalHelpClaimDetails::getAdjournedHearingFee),
  CMRH_ORAL(LegalHelpClaimDetails::getCmrhOral),
  CMRH_TELEPHONE(LegalHelpClaimDetails::getCmrhTelephone),
  HOME_OFFICE_INTERVIEW(LegalHelpClaimDetails::getHomeOfficeInterview),
  SUBSTANTIVE_HEARING(LegalHelpClaimDetails::getSubstantiveHearing);

  private final Function<LegalHelpClaimDetails, Object> accessor;

  LegalHelpClaimDetailsViewField(Function<LegalHelpClaimDetails, Object> accessor) {
    this.accessor = accessor;
  }
}
