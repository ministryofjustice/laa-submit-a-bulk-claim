package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimDetailsMapperImpl;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimMapperHelper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Claim detail view factory test")
class ClaimDetailViewFactoryTest {

  private final ClaimDetailViewFactory factory =
      new ClaimDetailViewFactory(new ClaimDetailsMapperImpl());

  {
    ReflectionTestUtils.setField(
        ReflectionTestUtils.getField(factory, "claimDetailsMapper"),
        "claimMapperHelper",
        new ClaimMapperHelper());
  }

  @Test
  @DisplayName("Should dispatch a CRIME_LOWER claim to the crime lower view")
  void shouldDispatchCrimeLower() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    ClaimDetailView<?> result = factory.create(claimResponse, null);

    assertThat(result).isInstanceOf(CrimeClaimCaseView.class);
    assertThat(result.valueRows()).hasSize(CrimeClaimCaseView.VALUE_ROWS.size());
    assertThat(result.totalRows()).hasSize(CrimeClaimCaseView.TOTAL_ROWS.size());
    assertThat(result.valueRows().keySet().iterator().next())
        .isEqualTo(ClaimDetailsViewField.FIXED_FEE);
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
  @DisplayName("A field with no reported source shows null rather than a placeholder")
  void shouldFallBackToNotApplicableForMissingValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    CrimeClaimCaseView result = (CrimeClaimCaseView) factory.create(claimResponse, null);

    ClaimField fixedFeeField = (ClaimField) result.valueRows().get(ClaimDetailsViewField.FIXED_FEE);

    assertThat(fixedFeeField.reported()).isNull();
  }

  @Test
  @DisplayName(
      "Should populate assessed from the given assessment, preserving reported and"
          + " initial calculated")
  void shouldMergeCurrentAssessmentIntoRows() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    AssessmentGet assessment =
        new AssessmentGet()
            .fixedFeeAmount(new BigDecimal("999.99"))
            .allowedTotalVat(new BigDecimal("42.00"))
            .allowedTotalInclVat(new BigDecimal("242.00"));

    CrimeClaimCaseView result = (CrimeClaimCaseView) factory.create(claimResponse, assessment);

    ClaimField fixedFeeValueField =
        (ClaimField) result.valueRows().get(ClaimDetailsViewField.FIXED_FEE);
    assertThat(fixedFeeValueField.assessed()).isEqualTo(new BigDecimal("999.99"));
    assertThat(fixedFeeValueField.initialCalculated()).isNotNull();

    ClaimField totalVatField = (ClaimField) result.totalRows().get(ClaimDetailsViewField.TOTAL_VAT);
    assertThat(totalVatField.assessed()).isEqualTo(new BigDecimal("42.00"));
    ClaimField totalInclVatField =
        (ClaimField) result.totalRows().get(ClaimDetailsViewField.TOTAL_INCLUDING_VAT);
    assertThat(totalInclVatField.assessed()).isEqualTo(new BigDecimal("242.00"));
  }

  @Test
  @DisplayName("A field with no assessment source stays absent even when an assessment is supplied")
  void shouldLeaveAssessedAbsentWithoutAnAssessmentSource() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);
    claimResponse.setIsLondonRate(true);
    AssessmentGet assessment = new AssessmentGet().fixedFeeAmount(new BigDecimal("1.00"));

    LegalHelpClaimCaseView result =
        (LegalHelpClaimCaseView) factory.create(claimResponse, assessment);

    ClaimField londonRateField =
        (ClaimField) result.valueRows().get(LegalHelpClaimDetailsViewField.LONDON_RATE);

    assertThat(londonRateField.reported()).isEqualTo(true);
    assertThat(londonRateField.assessed()).isNull();
  }

  @Test
  @DisplayName("The crime-specific travel costs field is populated from the assessment")
  void crimeLowerTravelCostsPopulatedFromAssessment() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    AssessmentGet assessment = new AssessmentGet().netTravelCostsAmount(new BigDecimal("321.00"));

    CrimeClaimCaseView result = (CrimeClaimCaseView) factory.create(claimResponse, assessment);

    ClaimField travelCostsField =
        (ClaimField) result.valueRows().get(CrimeLowerClaimDetailsViewField.TRAVEL_COSTS);
    assertThat(travelCostsField.assessed()).isEqualTo(new BigDecimal("321.00"));
  }
}
