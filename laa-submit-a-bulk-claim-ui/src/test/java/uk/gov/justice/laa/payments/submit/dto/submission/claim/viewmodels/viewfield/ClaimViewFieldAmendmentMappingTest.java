package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.payments.submit.helper.AmendedFieldsTestData.amended;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.CrimeClaimDetailsView;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.LegalHelpClaimDetailsView;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.MediationClaimDetailsView;

@DisplayName("Claim view field amendment mapping")
class ClaimViewFieldAmendmentMappingTest {

  private static final String IDENTIFIER_PATTERN =
      "^(claim|client|claimCase|claimSummaryFee)\\.[a-z][A-Za-z0-9]*(#[01])?$";

  private static final String UNRELATED_IDENTIFIER = "claim.someOtherField";

  private static final Set<String> NOT_AMENDABLE =
      Set.of(
          "OFFICE_ACCOUNT_NUMBER",
          "DATE_SUBMITTED",
          "AREA_OF_LAW",
          "FEE_CODE_DESCRIPTION",
          "CATEGORY_OF_LAW",
          "ESCAPE_CASE",
          "FIXED_FEE",
          "TOTAL_VAT",
          "TOTAL_INCLUDING_VAT");

  private static Stream<ClaimViewField<?>> displayedRows() {
    return Stream.<List<? extends ClaimViewField<?>>>of(
            LegalHelpClaimDetailsView.SUMMARY_ROWS,
            LegalHelpClaimDetailsView.VALUE_ROWS,
            LegalHelpClaimDetailsView.TOTAL_ROWS,
            CrimeClaimDetailsView.SUMMARY_ROWS,
            CrimeClaimDetailsView.VALUE_ROWS,
            CrimeClaimDetailsView.TOTAL_ROWS,
            MediationClaimDetailsView.SUMMARY_ROWS,
            MediationClaimDetailsView.VALUE_ROWS,
            MediationClaimDetailsView.TOTAL_ROWS)
        .<ClaimViewField<?>>flatMap(List::stream)
        .distinct();
  }

  private static Stream<ClaimViewField<?>> amendableRows() {
    return displayedRows().filter(field -> !field.getClaimsApiFieldNames().isEmpty());
  }

  @Test
  @DisplayName("Every displayed row either maps a claims API field or is declared not amendable")
  void everyDisplayedRowIsMappedOrDeclaredNotAmendable() {
    List<String> unaccounted =
        displayedRows()
            .filter(field -> field.getClaimsApiFieldNames().isEmpty())
            .map(ClaimViewField::name)
            .filter(name -> !NOT_AMENDABLE.contains(name))
            .toList();

    assertThat(unaccounted).isEmpty();
  }

  @Test
  @DisplayName("Every row declared not amendable is still displayed somewhere")
  void everyNotAmendableRowIsStillDisplayed() {
    List<String> displayed = displayedRows().map(ClaimViewField::name).toList();

    assertThat(displayed).containsAll(NOT_AMENDABLE);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("amendableRows")
  @DisplayName("Uses well formed claims API field identifiers")
  void usesWellFormedIdentifiers(ClaimViewField<?> field) {
    assertThat(field.getClaimsApiFieldNames()).allMatch(name -> name.matches(IDENTIFIER_PATTERN));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("amendableRows")
  @DisplayName("Is amended when any of its own claims API fields changed")
  void isAmendedWhenItsOwnFieldChanged(ClaimViewField<?> field) {
    for (String fieldName : field.getClaimsApiFieldNames()) {
      assertThat(field.isAmended(amended(fieldName))).isTrue();
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("displayedRows")
  @DisplayName("Is not amended when an unrelated claims API field changed")
  void isNotAmendedWhenUnrelatedFieldChanged(ClaimViewField<?> field) {
    assertThat(field.isAmended(amended(UNRELATED_IDENTIFIER))).isFalse();
  }
}
