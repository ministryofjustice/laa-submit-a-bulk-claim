package uk.gov.justice.laa.bulkclaim.service.claimdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.CrimeLowerClaimDetailsMapperImpl;
import uk.gov.justice.laa.bulkclaim.mapper.LegalHelpClaimDetailsMapperImpl;
import uk.gov.justice.laa.bulkclaim.mapper.MediationClaimDetailsMapperImpl;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Claim detail view factory test")
class ClaimDetailViewFactoryTest {

  private final ClaimDetailViewFactory factory =
      new ClaimDetailViewFactory(
          new CrimeLowerClaimDetailsMapperImpl(),
          new LegalHelpClaimDetailsMapperImpl(),
          new MediationClaimDetailsMapperImpl());

  @Test
  @DisplayName("Should dispatch a CRIME_LOWER claim to the crime lower view")
  void shouldDispatchCrimeLower() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    ClaimDetailView result = factory.build(claimResponse);

    assertThat(result).isInstanceOf(ClaimDetailView.CrimeLower.class);
    assertThat(result.template()).isEqualTo("pages/view-claim-detail-crime-lower");
    assertThat(result.valueRows()).hasSize(CrimeLowerClaimDetailsViewField.VALUE_ROWS.size());
  }

  @Test
  @DisplayName("Should dispatch a LEGAL_HELP claim to the legal help view")
  void shouldDispatchLegalHelp() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);

    ClaimDetailView result = factory.build(claimResponse);

    assertThat(result).isInstanceOf(ClaimDetailView.LegalHelp.class);
    assertThat(result.template()).isEqualTo("pages/view-claim-detail-legal-help");
  }

  @Test
  @DisplayName("Should dispatch a MEDIATION claim to the mediation view")
  void shouldDispatchMediation() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);

    ClaimDetailView result = factory.build(claimResponse);

    assertThat(result).isInstanceOf(ClaimDetailView.Mediation.class);
    assertThat(result.template()).isEqualTo("pages/view-claim-detail-mediation");
  }

  @Test
  @DisplayName("Should reject a claim with no area of law rather than silently defaulting")
  void shouldRejectMissingAreaOfLaw() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    claimResponse.setAreaOfLaw(null);

    assertThatThrownBy(() -> factory.build(claimResponse))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("A field with no reported source shows 'Not applicable' rather than null")
  void shouldFallBackToNotApplicableForMissingValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    ClaimDetailView.CrimeLower result = (ClaimDetailView.CrimeLower) factory.build(claimResponse);
    CrimeLowerClaimDetails details = result.details();

    ClaimFieldRow fixedFeeRow =
        (ClaimFieldRow) CrimeLowerClaimDetailsViewField.FIXED_FEE.getAccessor().apply(details);

    assertThat(fixedFeeRow.hasReportedValue()).isFalse();
    assertThat(fixedFeeRow.getReportedDisplay()).isEqualTo(ClaimFieldRow.NOT_APPLICABLE);
  }
}
