package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder.NEW_MATTER_STARTS_LABEL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CategoryCode;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MediationType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@ExtendWith(MockitoExtension.class)
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
  @DisplayName("Should map matter starts with category code - Legal Help")
  void shouldMapMatterStartsWithCategoryCodeLegalHelp() {
    // Given
    UUID submissionReference = UUID.fromString("162521bc-7d32-4f69-a661-15e668aae963");
    UUID matterStartsReference = UUID.fromString("c454ec6d-951a-4011-91fa-633870692de7");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionReference)
            .areaOfLaw(AreaOfLaw.LEGAL_HELP)
            .matterStarts(Collections.singletonList(matterStartsReference))
            .build();

    MatterStartGet matterStartCategoryCode =
        MatterStartGet.builder().categoryCode(CategoryCode.AAP).numberOfMatterStarts(2).build();
    MatterStartGet matterStartMediationType =
        MatterStartGet.builder()
            .mediationType(MediationType.MDAC_ALL_ISSUES_CO)
            .numberOfMatterStarts(3)
            .build();
    when(dataClaimsRestClient.getAllMatterStartsForSubmission(submissionReference))
        .thenReturn(
            Mono.just(
                MatterStartResultSet.builder()
                    .submissionId(submissionReference)
                    .matterStarts(Arrays.asList(matterStartCategoryCode, matterStartMediationType))
                    .build()));

    SubmissionMatterStartsRow expected = new SubmissionMatterStartsRow("Description", 25);
    when(mapper.toSubmissionMatterTypesRow(any())).thenReturn(expected);
    // When
    List<SubmissionMatterStartsRow> build = builder.build(submissionResponse);
    // Then
    assertThat(build.size()).isEqualTo(1);
    assertThat(build).contains(new SubmissionMatterStartsRow("Description", 25));
    verify(dataClaimsRestClient).getAllMatterStartsForSubmission(eq(submissionReference));
    verify(mapper, times(1)).toSubmissionMatterTypesRow(matterStartCategoryCode);
    verify(mapper, times(0)).toSubmissionMatterTypesRow(matterStartMediationType);
    verifyNoMoreInteractions(dataClaimsRestClient, mapper);
  }

  @Test
  @DisplayName("Should map matter starts with mediation type - Mediation")
  void shouldMapMatterStartsWithCategoryCodeMediation() {
    // Given
    UUID submissionReference = UUID.fromString("162521bc-7d32-4f69-a661-15e668aae963");
    UUID matterStartsReference = UUID.fromString("c454ec6d-951a-4011-91fa-633870692de7");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionReference)
            .areaOfLaw(AreaOfLaw.MEDIATION)
            .matterStarts(Collections.singletonList(matterStartsReference))
            .build();

    MatterStartGet matterStartCategoryCode =
        MatterStartGet.builder().categoryCode(CategoryCode.AAP).numberOfMatterStarts(2).build();
    MatterStartGet matterStartMediationType =
        MatterStartGet.builder()
            .mediationType(MediationType.MDAC_ALL_ISSUES_CO)
            .numberOfMatterStarts(3)
            .build();
    when(dataClaimsRestClient.getAllMatterStartsForSubmission(submissionReference))
        .thenReturn(
            Mono.just(
                MatterStartResultSet.builder()
                    .submissionId(submissionReference)
                    .matterStarts(Arrays.asList(matterStartCategoryCode, matterStartMediationType))
                    .build()));

    // When
    List<SubmissionMatterStartsRow> build = builder.build(submissionResponse);
    // Then
    assertThat(build.size()).isEqualTo(1);
    assertThat(build).contains(new SubmissionMatterStartsRow(NEW_MATTER_STARTS_LABEL, 3));
    verify(dataClaimsRestClient).getAllMatterStartsForSubmission(eq(submissionReference));
    verify(mapper, times(0)).toSubmissionMatterTypesRow(matterStartCategoryCode);
    verify(mapper, times(0)).toSubmissionMatterTypesRow(matterStartMediationType);
    verifyNoMoreInteractions(dataClaimsRestClient, mapper);
  }

  @Test
  @DisplayName("Should map matter starts with mediation type multiple - Mediation")
  void shouldMapMatterStartsWithMediationTypeMediation() {
    // Given
    UUID submissionReference = UUID.fromString("162521bc-7d32-4f69-a661-15e668aae963");
    UUID matterStartsReference = UUID.fromString("c454ec6d-951a-4011-91fa-633870692de7");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionReference)
            .areaOfLaw(AreaOfLaw.MEDIATION)
            .matterStarts(Collections.singletonList(matterStartsReference))
            .build();

    MatterStartGet matterStartMediationType =
        MatterStartGet.builder()
            .mediationType(MediationType.MDAC_ALL_ISSUES_CO)
            .numberOfMatterStarts(3)
            .build();
    MatterStartGet matterStartMediationTypeTwo =
        MatterStartGet.builder()
            .mediationType(MediationType.MDAS_ALL_ISSUES_SOLE)
            .numberOfMatterStarts(23)
            .build();
    when(dataClaimsRestClient.getAllMatterStartsForSubmission(submissionReference))
        .thenReturn(
            Mono.just(
                MatterStartResultSet.builder()
                    .submissionId(submissionReference)
                    .matterStarts(
                        Arrays.asList(matterStartMediationType, matterStartMediationTypeTwo))
                    .build()));

    // When
    List<SubmissionMatterStartsRow> build = builder.build(submissionResponse);
    // Then
    assertThat(build.size()).isEqualTo(1);
    assertThat(build).contains(new SubmissionMatterStartsRow(NEW_MATTER_STARTS_LABEL, 26));
    verify(dataClaimsRestClient).getAllMatterStartsForSubmission(eq(submissionReference));
    verify(mapper, times(0)).toSubmissionMatterTypesRow(matterStartMediationType);
    verify(mapper, times(0)).toSubmissionMatterTypesRow(matterStartMediationTypeTwo);
    verifyNoMoreInteractions(dataClaimsRestClient, mapper);
  }

  @Test
  @DisplayName("Should return empty list when mediation matter starts total zero")
  void shouldReturnEmptyListWhenMediationMatterStartsZero() {
    // Given
    UUID submissionReference = UUID.fromString("162521bc-7d32-4f69-a661-15e668aae963");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionReference)
            .areaOfLaw(AreaOfLaw.MEDIATION)
            .build();

    when(dataClaimsRestClient.getAllMatterStartsForSubmission(submissionReference))
        .thenReturn(
            Mono.just(
                MatterStartResultSet.builder()
                    .submissionId(submissionReference)
                    .matterStarts(Collections.emptyList())
                    .build()));

    // When
    List<SubmissionMatterStartsRow> build = builder.build(submissionResponse);
    // Then
    assertThat(build).isEmpty();
    verify(dataClaimsRestClient).getAllMatterStartsForSubmission(eq(submissionReference));
    verifyNoMoreInteractions(dataClaimsRestClient, mapper);
  }
}
