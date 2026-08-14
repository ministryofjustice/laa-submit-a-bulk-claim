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
    UNIQUE_FILE_NUMBER(CrimeLowerClaimDetails::getUniqueFileNumber);

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
  @DisplayName("Should build an ordered field map using each field's accessor")
  void shouldBuildFieldMap() {
    CrimeLowerClaimDetails claim = new CrimeLowerClaimDetails();
    claim.setUniqueFileNumber("ufn");

    LinkedHashMap<TestField, Object> result =
        ClaimViewField.toFieldMap(List.of(TestField.UNIQUE_FILE_NUMBER).stream(), claim);

    assertThat(result).containsExactly(Map.entry(TestField.UNIQUE_FILE_NUMBER, "ufn"));
  }
}
