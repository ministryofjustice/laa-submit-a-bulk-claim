package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.payments.submit.helper.AmendedFieldsTestData.amended;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;

@DisplayName("Claim view field test")
class ClaimViewFieldTest {

  private enum TestField implements ClaimViewField<CrimeLowerClaimDetails> {
    UNIQUE_FILE_NUMBER(CrimeLowerClaimDetails::getUniqueFileNumber, "claim.uniqueFileNumber"),
    CLIENT_NAME(
        CrimeLowerClaimDetails::clientName, "client.clientForename", "client.clientSurname"),
    AREA_OF_LAW(CrimeLowerClaimDetails::getAreaOfLaw);

    private final Function<CrimeLowerClaimDetails, Object> accessor;
    private final Set<String> claimsApiFieldNames;

    TestField(Function<CrimeLowerClaimDetails, Object> accessor, String... claimsApiFieldNames) {
      this.accessor = accessor;
      this.claimsApiFieldNames = Set.of(claimsApiFieldNames);
    }

    @Override
    public Function<CrimeLowerClaimDetails, Object> getAccessor() {
      return accessor;
    }

    @Override
    public Set<String> getClaimsApiFieldNames() {
      return claimsApiFieldNames;
    }
  }

  @Test
  @DisplayName("Should build an ordered field map using each field's accessor")
  void shouldBuildFieldMap() {
    CrimeLowerClaimDetails claim = new CrimeLowerClaimDetails();
    claim.setUniqueFileNumber("ufn");

    LinkedHashMap<TestField, Object> result =
        ClaimViewField.toFieldMap(Stream.of(TestField.UNIQUE_FILE_NUMBER), claim);

    assertThat(result).containsExactly(Map.entry(TestField.UNIQUE_FILE_NUMBER, "ufn"));
  }

  @Test
  @DisplayName("Should report a field as amended when its claim field changed")
  void shouldReportFieldAsAmended() {
    assertThat(TestField.UNIQUE_FILE_NUMBER.isAmended(amended("claim.uniqueFileNumber"))).isTrue();
  }

  @Test
  @DisplayName("Should report a multi field row as amended when any of its claim fields changed")
  void shouldReportMultiFieldRowAsAmended() {
    assertThat(TestField.CLIENT_NAME.isAmended(amended("client.clientSurname"))).isTrue();
  }

  @Test
  @DisplayName("Should not report a field as amended when another field changed")
  void shouldNotReportUnchangedFieldAsAmended() {
    assertThat(TestField.UNIQUE_FILE_NUMBER.isAmended(amended("claim.feeCode"))).isFalse();
  }

  @Test
  @DisplayName("Should not report a field with no claims API field as amended")
  void shouldNotReportDerivedFieldAsAmended() {
    assertThat(TestField.AREA_OF_LAW.isAmended(amended("claim.feeCode"))).isFalse();
  }

  @Test
  @DisplayName("Should not report a field as amended when there are no amended fields")
  void shouldNotReportFieldAsAmendedWhenNoAmendments() {
    assertThat(TestField.UNIQUE_FILE_NUMBER.isAmended(Set.of())).isFalse();
    assertThat(TestField.UNIQUE_FILE_NUMBER.isAmended(null)).isFalse();
  }
}
