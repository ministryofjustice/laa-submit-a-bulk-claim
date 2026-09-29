package uk.gov.justice.laa.payments.submit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.service.SubmissionService;

@WebMvcTest(ExportSubmissionDetailController.class)
@AutoConfigureMockMvc
@DisplayName("Export Submission Detail Controller Tests")
class ExportSubmissionDetailControllerTest extends BaseControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private ExportDataClaimsRestClient exportDataClaimsRestClient;

  @MockitoBean private SubmissionService submissionService;

  @Nested
  @DisplayName("GET: /submissions/{submissionId}/export")
  class GetExportSubmission {

    @BeforeEach
    void setUp() {
      when(submissionService.getSubmission(any(), any()))
          .thenReturn(succeededSubmission("12345", AreaOfLaw.LEGAL_HELP, "MAY-2026"));
    }

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      // Given
      String fileContent = "one,two,three";
      byte[] file = fileContent.getBytes();
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(Mono.just(ResponseEntity.ok(file)));

      // When (first request starts async processing due to controller method using "Mono")
      var initial =
          mockMvc.perform(
              get("/submissions/%s/export".formatted(submissionReference))
                  .with(oidcLogin().oidcUser(OIDC_USER)));

      // When / Then
      assertThat(mockMvc.perform(asyncDispatch(initial.getMvcResult())))
          .hasStatusOk()
          .body()
          .asString()
          .isEqualTo(fileContent);
    }

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = "VALIDATION_SUCCEEDED",
        mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should return not found when the submission is not available for export")
    void shouldReturnNotFoundWhenSubmissionIsNotAvailableForExport(SubmissionStatus status) {
      // Given
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(submissionService.getSubmission(eq(submissionReference), any()))
          .thenReturn(SubmissionResponse.builder().status(status).build());

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/submissions/%s/export".formatted(submissionReference))
                      .with(oidcLogin().oidcUser(OIDC_USER))))
          .hasStatus(HttpStatus.NOT_FOUND);
      verify(exportDataClaimsRestClient, never()).getSubmissionExport(any(), any(), any());
    }

    @ParameterizedTest
    @CsvSource({
      "LEGAL_HELP, legal-help",
      "CRIME_LOWER, crime-lower",
      "MEDIATION, mediation",
    })
    @DisplayName("Should request the export for the area of law path variable")
    void shouldRequestExportForAreaOfLawPathVariable(
        AreaOfLaw areaOfLaw, String expectedPathVariable) {
      // Given
      String office = "12345";
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(submissionService.getSubmission(eq(submissionReference), any()))
          .thenReturn(succeededSubmission(office, areaOfLaw, "MAY-2026"));
      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(Mono.just(ResponseEntity.ok("one,two,three".getBytes())));

      // When
      var initial =
          mockMvc.perform(
              get("/submissions/%s/export".formatted(submissionReference))
                  .with(oidcLogin().oidcUser(OIDC_USER)));

      // Then
      assertThat(mockMvc.perform(asyncDispatch(initial.getMvcResult()))).hasStatusOk();
      verify(exportDataClaimsRestClient)
          .getSubmissionExport(eq(expectedPathVariable), eq(submissionReference), eq(office));
    }

    @ParameterizedTest
    @CsvSource({
      "LEGAL_HELP, 2B446C-legal-help-2026-may-bulk-claim-summary.csv",
      "CRIME_LOWER, 2B446C-crime-lower-2026-may-bulk-claim-summary.csv",
      "MEDIATION, 2B446C-mediation-2026-may-bulk-claim-summary.csv",
    })
    @DisplayName("Should return the content type and file name")
    void shouldReturnContentTypeAndAreaOfLawFileName(AreaOfLaw areaOfLaw, String expectedFilename) {
      // Given
      String office = "2B446C";
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(submissionService.getSubmission(eq(submissionReference), any()))
          .thenReturn(succeededSubmission(office, areaOfLaw, "MAY-2026"));

      HttpHeaders claimsApiHeaders = new HttpHeaders();
      claimsApiHeaders.setContentType(MediaType.parseMediaType("text/csv"));
      claimsApiHeaders.setContentDisposition(
          ContentDisposition.attachment().filename("submission-claims-legal-help.csv").build());
      claimsApiHeaders.add("x-internal-header", "should-not-be-forwarded");

      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(
              Mono.just(
                  ResponseEntity.ok().headers(claimsApiHeaders).body("one,two,three".getBytes())));
      // When
      var initial =
          mockMvc.perform(
              get("/submissions/%s/export".formatted(submissionReference))
                  .with(oidcLogin().oidcUser(OIDC_USER)));

      // Then
      assertThat(mockMvc.perform(asyncDispatch(initial.getMvcResult())))
          .hasStatusOk()
          .headers()
          .satisfies(
              headers -> {
                assertThat(headers.getContentType())
                    .isEqualTo(MediaType.parseMediaType("text/csv"));
                assertThat(headers.getContentDisposition().getFilename())
                    .isEqualTo(expectedFilename);
                assertThat(headers.getContentDisposition().isAttachment()).isTrue();
                assertThat(headers.headerNames()).doesNotContain("x-internal-header");
              });
    }

    @ParameterizedTest
    @ValueSource(strings = {"/submission/%s/export", "/submissions/%s/export"})
    @DisplayName("Should export from both the singular and plural mappings")
    void shouldExportFromBothMappings(String mapping) {
      // Given
      String fileContent = "one,two,three";
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(Mono.just(ResponseEntity.ok(fileContent.getBytes())));

      // When
      var initial =
          mockMvc.perform(
              get(mapping.formatted(submissionReference)).with(oidcLogin().oidcUser(OIDC_USER)));

      // Then
      assertThat(mockMvc.perform(asyncDispatch(initial.getMvcResult())))
          .hasStatusOk()
          .body()
          .asString()
          .isEqualTo(fileContent);
    }

    private SubmissionResponse succeededSubmission(
        String office, AreaOfLaw areaOfLaw, String submissionPeriod) {
      return SubmissionResponse.builder()
          .status(SubmissionStatus.VALIDATION_SUCCEEDED)
          .officeAccountNumber(office)
          .areaOfLaw(areaOfLaw)
          .submissionPeriod(submissionPeriod)
          .build();
    }
  }
}
