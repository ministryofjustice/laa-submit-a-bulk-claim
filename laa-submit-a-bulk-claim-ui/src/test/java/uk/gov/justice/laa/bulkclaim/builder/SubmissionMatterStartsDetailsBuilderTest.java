package uk.gov.justice.laa.bulkclaim.builder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@ExtendWith(MockitoExtension.class)
@Disabled("This test will need fixing after refactor work")
@DisplayName("Submission matter starts builder tests")
class SubmissionMatterStartsDetailsBuilderTest {

  private SubmissionMatterStartsDetailsBuilder builder;

  @Mock DataClaimsRestClient dataClaimsRestClient;
  @Mock SubmissionMatterStartsMapper mapper;

  @BeforeEach
  void beforeEach() {
    this.builder = new SubmissionMatterStartsDetailsBuilder(dataClaimsRestClient, mapper);
  }

  @Test
  @DisplayName("Should map matter starts")
  void shouldMapMatterStarts() {
    // Given
    UUID submissionReference = UUID.fromString("162521bc-7d32-4f69-a661-15e668aae963");
    UUID matterStartsReference = UUID.fromString("c454ec6d-951a-4011-91fa-633870692de7");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionReference)
            .matterStarts(Collections.singletonList(matterStartsReference))
            .build();

    when(dataClaimsRestClient.getAllMatterStartsForSubmission(submissionReference))
        .thenReturn(
            Mono.just(
                MatterStartResultSet.builder()
                    .submissionId(submissionReference)
                    .matterStarts(Collections.singletonList(MatterStartGet.builder().build()))
                    .build()));

    SubmissionMatterStartsRow expected = new SubmissionMatterStartsRow("Description", 25);
    when(mapper.toSubmissionMatterTypesRow(any())).thenReturn(expected);
    // When
    List<SubmissionMatterStartsRow> build = builder.build(submissionResponse);
    // Then
    // assertThat(build.get(new SubmissionMatterStartsRow("Description", 25)))
    //    .isEqualTo(1L);
    verify(dataClaimsRestClient).getAllMatterStartsForSubmission(eq(submissionReference));
    verify(mapper).toSubmissionMatterTypesRow(eq(MatterStartGet.builder().build()));
    verifyNoMoreInteractions(dataClaimsRestClient, mapper);
  }
}
