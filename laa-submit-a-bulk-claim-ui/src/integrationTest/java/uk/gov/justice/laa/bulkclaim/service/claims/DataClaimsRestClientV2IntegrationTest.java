package uk.gov.justice.laa.bulkclaim.service.claims;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.helper.MockServerIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataClaimsRestClientV2IntegrationTest extends MockServerIntegrationTest {
  private static final String API_VERSION_V2 = "/api/v2/";
  private static final String GET_CLAIMS_V2_URI = API_VERSION_V2 + "claims";
  private static final String OFFICE_CODE = "office_code";
  private static final String OFFICE_CODE_VALUE = "0P322F";
  private static final String SUBMISSION_ID = "submission_id";
  private static final UUID SUBMISSION_ID_VALUE =
      UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
  private static final String PAGE = "page";
  private static final String PAGE_VALUE = "0";
  private static final String SIZE = "size";
  private static final String SIZE_VALUE = "20";
  private static final String SORT = "sort";

  private DataClaimsRestClientV2 dataClaimsRestClientV2;

  @BeforeEach
  public void initialize() {
    dataClaimsRestClientV2 = createClient(DataClaimsRestClientV2.class);
  }

  @Nested
  @DisplayName("GET: /api/v2/claims")
  class GetClaimsV2 {

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() throws Exception {
      var expectJson = readJsonFromFile("/GetClaimV2_200.json");

      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod(HttpMethod.GET.toString())
                  .withPath(GET_CLAIMS_V2_URI)
                  .withQueryStringParameters(
                      List.of(
                          Parameter.param(OFFICE_CODE, OFFICE_CODE_VALUE),
                          Parameter.param(SUBMISSION_ID, SUBMISSION_ID_VALUE.toString()),
                          Parameter.param(PAGE, PAGE_VALUE),
                          Parameter.param(SIZE, SIZE_VALUE),
                          Parameter.param(SORT, "client.clientForename,asc"))))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                  .withBody(expectJson));

      var actualResults =
          dataClaimsRestClientV2.getClaims(
              OFFICE_CODE_VALUE, SUBMISSION_ID_VALUE, 0, 20, "client.clientForename,asc");

      assertEquals(HttpStatus.OK, actualResults.getStatusCode());
      var result = objectMapper.writeValueAsString(actualResults.getBody());
      assertThatJsonMatches(expectJson, result);
    }
  }
}
