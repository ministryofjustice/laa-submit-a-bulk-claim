package uk.gov.justice.laa.payments.submit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.util.OidcAttributeUtils;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

  private static final UUID SUBMISSION_ID = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
  private static final String OFFICE_CODE = "123456";

  @Mock private DataClaimsRestClient dataClaimsRestClient;
  @Mock private OidcUser oidcUser;

  private SubmissionService submissionService;

  @BeforeEach
  void setUp() {
    submissionService = new SubmissionService(dataClaimsRestClient, new OidcAttributeUtils());
  }

  @Test
  void getSubmission_returnsSubmission_whenOfficeAccessAllowed() {
    var submission =
        SubmissionResponse.builder()
            .submissionId(SUBMISSION_ID)
            .officeAccountNumber(OFFICE_CODE)
            .build();
    when(dataClaimsRestClient.getSubmission(SUBMISSION_ID)).thenReturn(Mono.just(submission));
    when(oidcUser.getAttributes()).thenReturn(Map.of("LAA_ACCOUNTS", List.of(OFFICE_CODE)));

    var result = submissionService.getSubmission(SUBMISSION_ID, oidcUser);

    assertThat(result).isEqualTo(submission);
  }

  @Test
  void getSubmission_throwsNotFoundWhenSubmissionDoesNotExist() {
    when(dataClaimsRestClient.getSubmission(SUBMISSION_ID)).thenReturn(Mono.empty());

    var exception =
        assertThrows(
            ResponseStatusException.class,
            () -> submissionService.getSubmission(SUBMISSION_ID, oidcUser));

    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(exception.getMessage())
        .isEqualTo("404 NOT_FOUND \"Submission %s does not exist\"".formatted(SUBMISSION_ID));
  }

  @Test
  void getSubmission_throwsNotFoundWhenOfficeAccessIsForbidden() {
    var submission =
        SubmissionResponse.builder()
            .submissionId(SUBMISSION_ID)
            .officeAccountNumber(OFFICE_CODE)
            .build();
    when(dataClaimsRestClient.getSubmission(SUBMISSION_ID)).thenReturn(Mono.just(submission));
    when(oidcUser.getAttributes()).thenReturn(Map.of("LAA_ACCOUNTS", List.of("Different")));
    when(oidcUser.getAttribute("oid")).thenReturn("1111111111");

    var exception =
        assertThrows(
            ResponseStatusException.class,
            () -> submissionService.getSubmission(SUBMISSION_ID, oidcUser));

    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(exception.getMessage())
        .isEqualTo("404 NOT_FOUND \"User 1111111111 does not have access to office 123456\"");
  }
}
