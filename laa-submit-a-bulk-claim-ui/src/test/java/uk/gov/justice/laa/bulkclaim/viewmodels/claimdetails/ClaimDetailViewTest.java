package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;

@DisplayName("Claim detail view test")
class ClaimDetailViewTest {

  private record TestClaimDetailView(LinkedHashMap<ClaimViewField<?>, Object> summaryRows)
      implements ClaimDetailView<ClaimViewField<?>> {

    @Override
    public LinkedHashMap<ClaimViewField<?>, Object> valueRows() {
      return new LinkedHashMap<>();
    }

    @Override
    public LinkedHashMap<ClaimViewField<?>, Object> totalRows() {
      return new LinkedHashMap<>();
    }

    @Override
    public String pageTitle() {
      return "test";
    }
  }

  @Test
  @DisplayName("getUfn() should read the unique file number from the shared summary row")
  void shouldReadUniqueFileNumber() {
    LinkedHashMap<ClaimViewField<?>, Object> summaryRows = new LinkedHashMap<>();
    summaryRows.put(ClaimDetailsViewField.UNIQUE_FILE_NUMBER, "271219/000");
    ClaimDetailView<?> view = new TestClaimDetailView(summaryRows);

    assertThat(view.getUfn()).isEqualTo("271219/000");
  }

  @Test
  @DisplayName("getUfn() should return null rather than throw when the row is absent")
  void shouldReturnNullUniqueFileNumberWhenAbsent() {
    ClaimDetailView<?> view = new TestClaimDetailView(new LinkedHashMap<>());

    assertThat(view.getUfn()).isNull();
  }

  @Test
  @DisplayName("getFeeCode() should read the fee code from the shared summary row")
  void shouldReadFeeCode() {
    LinkedHashMap<ClaimViewField<?>, Object> summaryRows = new LinkedHashMap<>();
    summaryRows.put(ClaimDetailsViewField.FEE_CODE, "INVC");
    ClaimDetailView<?> view = new TestClaimDetailView(summaryRows);

    assertThat(view.getFeeCode()).isEqualTo("INVC");
  }

  @Test
  @DisplayName("getFeeCode() should return null rather than throw when the row is absent")
  void shouldReturnNullFeeCodeWhenAbsent() {
    ClaimDetailView<?> view = new TestClaimDetailView(new LinkedHashMap<>());

    assertThat(view.getFeeCode()).isNull();
  }
}
