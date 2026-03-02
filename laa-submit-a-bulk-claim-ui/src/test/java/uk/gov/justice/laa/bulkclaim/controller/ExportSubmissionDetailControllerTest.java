package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

@WebMvcTest(ExportSubmissionDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Export Submission Detail Controller Tests")
class ExportSubmissionDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private ExportDataClaimsRestClient exportDataClaimsRestClient;
  @MockitoBean private OidcAttributeUtils oidcAttributeUtils;

  @Nested
  @DisplayName("GET: /submission/{submissionId}/export")
  class GetExportSubmission {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      // Given
      String fileContent = "one,two,three";
      byte[] file = fileContent.getBytes();
      String office = "12345";
      String areaOfLaw = "legal-help";
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(Mono.just(ResponseEntity.ok(file)));
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of(office));

      // When (first request starts async processing due to controller method using "Mono")
      var initial =
          mockMvc.perform(
              get("/submission/%s/export?office=%s&areaOfLaw=%s"
                      .formatted(submissionReference, office, areaOfLaw))
                  .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser())));

      // When / Then
      assertThat(mockMvc.perform(asyncDispatch(initial.getMvcResult())))
          .hasStatusOk()
          .body()
          .asString()
          .isEqualTo(fileContent);
    }

    @Test
    @DisplayName("Should throw exception")
    void shouldThrowException() {
      // Given
      String fileContent = "one,two,three";
      byte[] file = fileContent.getBytes();
      String office = "12345";
      String areaOfLaw = "legal-help";
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(Mono.just(ResponseEntity.ok(file)));
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(Collections.emptyList());

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/submission/%s/export?office=%s&areaOfLaw=%s"
                          .formatted(submissionReference, office, areaOfLaw))
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("User (test@example.com) does not have access to office: 12345");
    }
  }
}
