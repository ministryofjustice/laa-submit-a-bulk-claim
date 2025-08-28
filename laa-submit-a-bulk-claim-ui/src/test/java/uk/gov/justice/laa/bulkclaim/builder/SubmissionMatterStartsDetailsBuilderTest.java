package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.MatterStartsGet;
import uk.gov.justice.laa.claims.model.SubmissionFields;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission matter starts builder tests")
class SubmissionMatterStartsDetailsBuilderTest {

  private SubmissionMatterStartsDetailsBuilder builder;

  @Mock DataClaimsRestService dataClaimsRestService;
  @Mock SubmissionMatterStartsMapper mapper;

  @BeforeEach
  void beforeEach() {
    this.builder = new SubmissionMatterStartsDetailsBuilder(dataClaimsRestService, mapper);
  }

  @Test
  @DisplayName("Should map matter starts")
  void shouldMapMatterStarts() {
    // Given
    UUID submissionReference = UUID.fromString("162521bc-7d32-4f69-a661-15e668aae963");
    UUID matterStartsReference = UUID.fromString("c454ec6d-951a-4011-91fa-633870692de7");
    GetSubmission200Response getSubmission200Response =
        GetSubmission200Response.builder()
            .submission(SubmissionFields.builder().submissionId(submissionReference).build())
            .matterStarts(Collections.singletonList(matterStartsReference))
            .build();
    when(dataClaimsRestService.getSubmissionMatterStarts(
            submissionReference, matterStartsReference))
        .thenReturn(Mono.just(MatterStartsGet.builder().build()));
    SubmissionMatterStartsRow expected = new SubmissionMatterStartsRow("Description");
    when(mapper.toSubmissionMatterTypesRow(any())).thenReturn(expected);
    // When
    SubmissionMatterStartsDetails build = builder.build(getSubmission200Response);
    // Then
    assertThat(build.matterTypes().get(new SubmissionMatterStartsRow("Description"))).isEqualTo(1L);
  }
}
