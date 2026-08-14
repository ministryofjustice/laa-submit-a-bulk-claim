package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;

@DisplayName("Claim view field test")
class ClaimViewFieldTest {

  private enum TestField implements ClaimViewField<CrimeLowerClaimDetails> {
    UNIQUE_FILE_NUMBER(CrimeLowerClaimDetails::uniqueFileNumber);

    private final Function<CrimeLowerClaimDetails, Object> accessor;

    TestField(Function<CrimeLowerClaimDetails, Object> accessor) {
      this.accessor = accessor;
    }

    @Override
    public Function<CrimeLowerClaimDetails, Object> getAccessor() {
      return accessor;
    }
  }

  @Test
  @DisplayName("Should default getReportedAndCalculatedAccessor and getCurrentCalculatedAccessor "
      + "to null when unimplemented")
  void shouldDefaultLegacyAccessorsToNull() {
    assertThat(TestField.UNIQUE_FILE_NUMBER.getReportedAndCalculatedAccessor()).isNull();
    assertThat(TestField.UNIQUE_FILE_NUMBER.getCurrentCalculatedAccessor()).isNull();
  }

  @Test
  @DisplayName("Should default getAccessor to getReportedAndCalculatedAccessor when unimplemented")
  void shouldDefaultGetAccessorToReportedAndCalculatedAccessor() {
    ClaimViewField<CrimeLowerClaimDetails> field =
        new ClaimViewField<>() {
          @Override
          public String name() {
            return "FIELD";
          }

          @Override
          public Function<CrimeLowerClaimDetails, Object> getReportedAndCalculatedAccessor() {
            return CrimeLowerClaimDetails::uniqueFileNumber;
          }
        };

    CrimeLowerClaimDetails claim = CrimeLowerClaimDetails.builder().uniqueFileNumber("ufn").build();

    assertThat(field.getAccessor().apply(claim)).isEqualTo("ufn");
  }

  @Test
  @DisplayName("Should build an ordered field map using each field's accessor")
  void shouldBuildFieldMap() {
    CrimeLowerClaimDetails claim = CrimeLowerClaimDetails.builder().uniqueFileNumber("ufn").build();

    LinkedHashMap<TestField, Object> result =
        ClaimViewField.toFieldMap(List.of(TestField.UNIQUE_FILE_NUMBER).stream(), claim);

    assertThat(result).containsExactly(Map.entry(TestField.UNIQUE_FILE_NUMBER, "ufn"));
  }
}
