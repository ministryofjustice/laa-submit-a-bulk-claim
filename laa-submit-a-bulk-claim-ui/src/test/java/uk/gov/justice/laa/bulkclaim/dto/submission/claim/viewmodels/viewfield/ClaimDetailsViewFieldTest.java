package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@DisplayName("Claim details view field test")
class ClaimDetailsViewFieldTest {

  private static final class TestClaimDetails extends ClaimDetails {}

  @Test
  @DisplayName("Should read every common summary field via its accessor")
  void shouldReadSummaryFields() {
    ClaimDetails claim = new TestClaimDetails();
    claim.setClientForename("forename");
    claim.setClientSurname("surname");
    claim.setUniqueFileNumber("ufn");
    claim.setOfficeCode("office-code");
    claim.setDateSubmitted(OffsetDateTime.parse("2026-01-15T10:15:30+00:00"));
    claim.setAreaOfLaw(AreaOfLaw.LEGAL_HELP);
    claim.setFeeCode("fee-code");
    claim.setFeeCodeDescription("fee-code-description");
    claim.setMatterTypeCode("matter-type-code");
    claim.setCaseStartDate("2025-09-19");
    claim.setCaseConcludedDate("2026-10-20");
    claim.setEscapeCase(true);

    assertThat(ClaimDetailsViewField.CLIENT_NAME.getAccessor().apply(claim))
        .isEqualTo("forename surname");
    assertThat(ClaimDetailsViewField.UNIQUE_FILE_NUMBER.getAccessor().apply(claim))
        .isEqualTo("ufn");
    assertThat(ClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER.getAccessor().apply(claim))
        .isEqualTo("office-code");
    assertThat(ClaimDetailsViewField.DATE_SUBMITTED.getAccessor().apply(claim))
        .isEqualTo(claim.getDateSubmitted());
    assertThat(ClaimDetailsViewField.AREA_OF_LAW.getAccessor().apply(claim))
        .isEqualTo(AreaOfLaw.LEGAL_HELP);
    assertThat(ClaimDetailsViewField.FEE_CODE.getAccessor().apply(claim)).isEqualTo("fee-code");
    assertThat(ClaimDetailsViewField.FEE_CODE_DESCRIPTION.getAccessor().apply(claim))
        .isEqualTo("fee-code-description");
    assertThat(ClaimDetailsViewField.MATTER_TYPE.getAccessor().apply(claim))
        .isEqualTo("matter-type-code");
    assertThat(ClaimDetailsViewField.CASE_START_DATE.getAccessor().apply(claim))
        .isEqualTo("2025-09-19");
    assertThat(ClaimDetailsViewField.DATE_OF_WORK_CONCLUDED.getAccessor().apply(claim))
        .isEqualTo("2026-10-20");
    assertThat(ClaimDetailsViewField.ESCAPE_CASE.getAccessor().apply(claim)).isEqualTo(true);
  }

  @Test
  @DisplayName("Should read every common value field via its accessor")
  void shouldReadValueFields() {
    ClaimDetails claim = new TestClaimDetails();
    ClaimFieldRow fixedFee = new ClaimFieldRow(null, "fixed-fee", "assessed-fixed-fee");
    ClaimFieldRow profitCosts = new ClaimFieldRow("profit-costs", null, "assessed-profit-costs");
    ClaimFieldRow disbursements = new ClaimFieldRow("disbursements", "disb-calc", "assessed-disb");
    ClaimFieldRow disbursementsVat =
        new ClaimFieldRow("disb-vat", "disb-vat-calc", "assessed-disb-vat");
    ClaimFieldRow vat = new ClaimFieldRow(true, true, false);
    ClaimFieldRow totalVat = new ClaimFieldRow(null, "total-vat-calc", "assessed-total-vat");
    ClaimFieldRow totalIncludingVat =
        new ClaimFieldRow(null, "total-incl-vat-calc", "assessed-total-incl-vat");
    claim.setFixedFee(fixedFee);
    claim.setProfitCosts(profitCosts);
    claim.setDisbursements(disbursements);
    claim.setDisbursementsVat(disbursementsVat);
    claim.setVat(vat);
    claim.setTotalVat(totalVat);
    claim.setTotalIncludingVat(totalIncludingVat);

    assertThat(ClaimDetailsViewField.FIXED_FEE.getAccessor().apply(claim)).isEqualTo(fixedFee);
    assertThat(ClaimDetailsViewField.PROFIT_COSTS.getAccessor().apply(claim))
        .isEqualTo(profitCosts);
    assertThat(ClaimDetailsViewField.DISBURSEMENTS.getAccessor().apply(claim))
        .isEqualTo(disbursements);
    assertThat(ClaimDetailsViewField.DISBURSEMENTS_VAT.getAccessor().apply(claim))
        .isEqualTo(disbursementsVat);
    assertThat(ClaimDetailsViewField.VAT.getAccessor().apply(claim)).isEqualTo(vat);
    assertThat(ClaimDetailsViewField.TOTAL_VAT.getAccessor().apply(claim)).isEqualTo(totalVat);
    assertThat(ClaimDetailsViewField.TOTAL_INCLUDING_VAT.getAccessor().apply(claim))
        .isEqualTo(totalIncludingVat);
  }

  @Test
  @DisplayName("Should be castable to an area-specific ClaimViewField via the unsafe-cast helpers")
  void shouldBeCastableToAreaSpecificViewField() {
    assertThat(ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.CLIENT_NAME))
        .isEqualTo(ClaimDetailsViewField.CLIENT_NAME);
    assertThat(ClaimViewField.asLegalHelpField(ClaimDetailsViewField.CLIENT_NAME))
        .isEqualTo(ClaimDetailsViewField.CLIENT_NAME);
    assertThat(ClaimViewField.asMediationField(ClaimDetailsViewField.CLIENT_NAME))
        .isEqualTo(ClaimDetailsViewField.CLIENT_NAME);
  }
}
