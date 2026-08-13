package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.CrimeLowerClaimDetailsMapperImpl;
import uk.gov.justice.laa.bulkclaim.mapper.LegalHelpClaimDetailsMapperImpl;
import uk.gov.justice.laa.bulkclaim.mapper.MediationClaimDetailsMapperImpl;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
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

    ClaimDetailView<?> result = factory.create(claimResponse, null);

    assertThat(result).isInstanceOf(CrimeClaimCaseView.class);
    assertThat(result.valueRows()).hasSize(CrimeClaimCaseView.VALUE_ROWS.size());
    assertThat(result.totalRows()).hasSize(CrimeClaimCaseView.TOTAL_ROWS.size());
    assertThat(result.valueRows().keySet().iterator().next())
        .isEqualTo(CrimeLowerClaimDetailsViewField.FIXED_FEE);
  }

  @Test
  @DisplayName("Should dispatch a LEGAL_HELP claim to the legal help view")
  void shouldDispatchLegalHelp() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);

    ClaimDetailView<?> result = factory.create(claimResponse, null);

    assertThat(result).isInstanceOf(LegalHelpClaimCaseView.class);
  }

  @Test
  @DisplayName("Should dispatch a MEDIATION claim to the mediation view")
  void shouldDispatchMediation() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);

    ClaimDetailView<?> result = factory.create(claimResponse, null);

    assertThat(result).isInstanceOf(MediationClaimCaseView.class);
  }

  @Test
  @DisplayName("Should reject a claim with no area of law rather than silently defaulting")
  void shouldRejectMissingAreaOfLaw() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    claimResponse.setAreaOfLaw(null);

    assertThatThrownBy(() -> factory.create(claimResponse, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("A field with no reported source shows 'Not applicable' rather than null")
  void shouldFallBackToNotApplicableForMissingValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    CrimeClaimCaseView result = (CrimeClaimCaseView) factory.create(claimResponse, null);

    ClaimFieldRow fixedFeeRow = result.valueRows().get(CrimeLowerClaimDetailsViewField.FIXED_FEE);

    assertThat(fixedFeeRow.hasReportedValue()).isFalse();
    assertThat(fixedFeeRow.getReportedDisplay()).isEqualTo(ClaimFieldRow.NOT_APPLICABLE);
  }

  @Test
  @DisplayName(
      "Should populate currentCalculated from the given assessment, preserving reported and"
          + " initial calculated")
  void shouldMergeCurrentAssessmentIntoRows() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    AssessmentGet assessment =
        new AssessmentGet()
            .fixedFeeAmount(new BigDecimal("999.99"))
            .allowedTotalVat(new BigDecimal("42.00"))
            .allowedTotalInclVat(new BigDecimal("242.00"));

    CrimeClaimCaseView result = (CrimeClaimCaseView) factory.create(claimResponse, assessment);

    ClaimFieldRow fixedFeeValueRow =
        result.valueRows().get(CrimeLowerClaimDetailsViewField.FIXED_FEE);
    assertThat(fixedFeeValueRow.currentCalculated()).isEqualTo(new BigDecimal("999.99"));
    assertThat(fixedFeeValueRow.initialCalculated()).isNotNull();

    ClaimFieldRow totalVatRow = result.totalRows().get(CrimeLowerClaimDetailsViewField.TOTAL_VAT);
    assertThat(totalVatRow.currentCalculated()).isEqualTo(new BigDecimal("42.00"));
    ClaimFieldRow totalInclVatRow =
        result.totalRows().get(CrimeLowerClaimDetailsViewField.TOTAL_INCLUDING_VAT);
    assertThat(totalInclVatRow.currentCalculated()).isEqualTo(new BigDecimal("242.00"));
  }

  @Test
  @DisplayName(
      "A field with no assessment accessor stays absent even when an assessment is supplied")
  void shouldLeaveCurrentCalculatedAbsentWithoutAnAssessmentAccessor() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);
    AssessmentGet assessment = new AssessmentGet().fixedFeeAmount(new BigDecimal("1.00"));

    LegalHelpClaimCaseView result =
        (LegalHelpClaimCaseView) factory.create(claimResponse, assessment);

    ClaimFieldRow londonRateRow =
        result.valueRows().get(LegalHelpClaimDetailsViewField.LONDON_RATE);
    assertThat(londonRateRow.hasCurrentCalculatedValue()).isFalse();
  }
}
