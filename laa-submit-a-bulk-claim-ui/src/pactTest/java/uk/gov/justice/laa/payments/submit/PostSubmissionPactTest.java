package uk.gov.justice.laa.payments.submit;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.payments.submit.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.payments.submit.dto.NilSubmissionResult;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.payments.submit.service.NilSubmissionService;

/**
 * Pact for creating a submission, driven through {@link NilSubmissionService} so the request body
 * in the contract is the one the nil-submission journey really sends.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1238"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1238") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("POST: /api/v1/submissions PACT tests")
public final class PostSubmissionPactTest extends AbstractPactTest {

  @Autowired NilSubmissionService nilSubmissionService;

  @Pact(consumer = CONSUMER)
  public RequestResponsePact postSubmission201(PactDslWithProvider builder) {
    return builder
        .given("the system is ready to process a valid submission")
        .uponReceiving("a request to create a nil submission")
        .path("/api/v1/submissions")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .matchHeader(HttpHeaders.CONTENT_TYPE, "application/json.*", "application/json")
        .method("POST")
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.uuid("submission_id", SUBMISSION_ID);
                      body.stringMatcher("office_account_number", OFFICE_CODE_REGEX, "0P322F");
                      body.stringMatcher("submission_period", "[A-Z]{3}-\\d{4}", "MAY-2025");
                      body.stringValue("area_of_law", "LEGAL HELP");
                      body.stringType("provider_user_id", "provider@example.com");
                      body.stringValue("status", "READY_FOR_VALIDATION");
                      body.stringType("legal_help_submission_reference", "0P322F/Civil/01");
                      body.booleanValue("is_nil_submission", true);
                      body.numberValue("number_of_claims", 0);
                      body.stringType("created_by_user_id", "Submit-a-bulk-claim");
                      // Unset fields are serialised as explicit nulls by the UI
                      List.of(
                              "bulk_submission_id",
                              "previous_submission_id",
                              "crime_lower_schedule_number",
                              "mediation_submission_reference",
                              "calculated_total_amount",
                              "assessed_total_amount",
                              "submitted",
                              "error_messages")
                          .forEach(body::nullValue);
                    })
                .build())
        .willRespondWith()
        .status(201)
        .headers(Map.of("Content-Type", "application/json"))
        .body(LambdaDsl.newJsonBody(body -> body.uuid("id", SUBMISSION_ID)).build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 201 response")
  @PactTestFor(pactMethod = "postSubmission201")
  void verify201Response() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("0P322F");
    form.setAreaOfLaw(AreaOfLaw.LEGAL_HELP);
    form.setSubmissionPeriod("MAY-2025");
    form.setSubmissionReference("0P322F/Civil/01");

    NilSubmissionResult result = nilSubmissionService.createSubmission(form, oidcUser());

    assertThat(result.errorMessages()).isEmpty();
    assertThat(result.submissionId()).isNotNull();
  }

  private static OidcUser oidcUser() {
    OidcIdToken idToken =
        new OidcIdToken(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("sub", "user", "preferred_username", "provider@example.com"));
    return new DefaultOidcUser(List.of(), idToken, "preferred_username");
  }
}
