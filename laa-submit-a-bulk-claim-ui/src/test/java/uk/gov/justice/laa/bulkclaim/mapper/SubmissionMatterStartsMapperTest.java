package uk.gov.justice.laa.bulkclaim.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CategoryCode;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;

@ExtendWith(SpringExtension.class)
@DisplayName("Submission matter starts mapper tests")
class SubmissionMatterStartsMapperTest {

  private SubmissionMatterStartsMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SubmissionMatterStartsMapperImpl();
  }

  @Test
  @DisplayName("Should map submission matter starts")
  void shouldMapSubmissionMatterStarts() {
    // Given
    MatterStartGet matterStartsFields =
        MatterStartGet.builder()
            .scheduleReference("Schedule Reference")
            .categoryCode(CategoryCode.AAP)
            .procurementAreaCode("Procurement Area Code")
            .accessPointCode("Access Point Code")
            .deliveryLocation("Delivery Location")
            .numberOfMatterStarts(23)
            .build();
    // When
    SubmissionMatterStartsRow result = mapper.toSubmissionMatterTypesRow(matterStartsFields);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions.assertThat(result.description()).isEqualTo(CategoryCode.AAP.toString());
          softAssertions.assertThat(result.numberOfMatterStarts()).isEqualTo(23);
        });
  }
}
