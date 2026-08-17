package uk.gov.justice.laa.payments.submit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.payments.submit.metrics.BulkClaimMetricService;

@WebMvcTest(BulkUploadBeingCheckedController.class)
@AutoConfigureMockMvc
public class BulkUploadBeingCheckedControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;

  @MockitoBean private BulkClaimMetricService bulkClaimMetricService;

  @Nested
  @DisplayName("GET: /upload-is-being-checked")
  class UploadIsBeingChecked {

    @ParameterizedTest
    @EnumSource(
        value = BulkSubmissionStatus.class,
        names = {"READY_FOR_PARSING", "PARSING_COMPLETED"})
    @DisplayName("Should return expected result bulk submission is not ready")
    void shouldReturnExpectedResult(BulkSubmissionStatus status) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenReturn(
              Mono.just(GetBulkSubmissionStatusById200Response.builder().status(status).build()));
      assertThat(
          mockMvc.perform(
              get("/upload-is-being-checked")
                  .with(oidcLogin().oidcUser(OIDC_USER))
                  .sessionAttr(SUBMISSION_ID, submissionId)
                  .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
