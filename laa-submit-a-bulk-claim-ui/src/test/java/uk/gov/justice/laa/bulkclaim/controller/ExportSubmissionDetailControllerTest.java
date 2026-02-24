package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@WebMvcTest(ExportSubmissionDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Export Submission Detail Controller Tests")
class ExportSubmissionDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private ExportDataClaimsRestClient exportDataClaimsRestClient;

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
      String submissionPeriod = "MAY-2020";
      UUID submissionReference = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      when(dataClaimsRestClient.getSubmission(submissionReference))
          .thenReturn(
              Mono.just(
                  SubmissionResponse.builder()
                      .officeAccountNumber(office)
                      .areaOfLaw(AreaOfLaw.LEGAL_HELP)
                      .submissionPeriod(submissionPeriod)
                      .build()));
      when(exportDataClaimsRestClient.getSubmissionExport(any(), any(), any()))
          .thenReturn(Mono.just(ResponseEntity.ok(file)));

      // When (first request starts async processing due to controller method using "Mono")
      var initial =
          mockMvc.perform(
              get("/submission/%s/export?office=%s&areaOfLaw=%s&submissionPeriod=%s"
                      .formatted(submissionReference, office, areaOfLaw, submissionPeriod))
                  .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser())));

      // When / Then
      assertThat(mockMvc.perform(asyncDispatch(initial.getMvcResult())))
          .hasStatusOk()
          .body()
          .asString()
          .isEqualTo(fileContent);
    }
  }
}
