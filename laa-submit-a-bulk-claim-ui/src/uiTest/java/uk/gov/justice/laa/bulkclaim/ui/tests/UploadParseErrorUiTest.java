package uk.gov.justice.laa.bulkclaim.ui.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.justice.laa.bulkclaim.ui.helpers.AbstractUiTest;
import uk.gov.justice.laa.bulkclaim.ui.pages.UploadPage;

/**
 * UI Tests for Legal Help parse error validations.
 *
 * In UI tests, local validators are mocked (see AbstractAccessibilityTest), so to verify upload-page
 * error rendering we stub Claims API upload responses to return ProblemDetail errors.
 */
@DisplayName("Upload Parse Error Validation UI Tests")
class UploadParseErrorUiTest extends AbstractUiTest {

  private UploadPage uploadPage;

  @BeforeEach
  void setUpPageObjects() {
    uploadPage = new UploadPage(page);
    uploadPage.open(appLandingUrl());
  }

  @ParameterizedTest(name = "{0} = {1} should show parse error: {2}")
  @CsvSource({
      "vatApplicable, A, VAT Applicable must only include Y or N",
      "postalApplication, A, Postal Application Accepted must only include Y or N",
      "nrmAdvice, A, NRM Advice must only include Y or N",
      "legacyCase, A, Legacy Case must only include Y or N",
      "londonNonLondonRate, A, London Rate must only include Y or N",
      "additionalTravelPayment, A, Additional Travel Payment must only include Y or N",
      "eligibleClientIndicator, A, Eligible Client must only include Y or N",
      "ircSurgery, A, IRC Surgery must only include Y or N",
      "substantiveHearing, A, Substantive Hearing must only include Y or N",
      "toleranceIndicator, A, Tolerance Applicable must only include Y or N",
      "caseStartDate, abc, Case Start Date must be a valid date in the format DD/MM/YYYY",
      "workConcludedDate, abc, Work Concluded Date must be a valid date in the format DD/MM/YYYY",
      "clientDateOfBirth, abc, Client Date of Birth must be a valid date in the format DD/MM/YYYY",
      "transferDate, abc, Transfer Date must be a valid date in the format DD/MM/YYYY",
      "surgeryDate, abc, Surgery Date must be a valid date in the format DD/MM/YYYY"
  })
  @DisplayName("should display parse error on upload page")
  void testParseErrorDisplayed(String fieldName, String value, String expectedError) throws IOException {
    Path fileWithError = generateMalformedCsv(fieldName, value);
    stubClaimsApiUploadFailure(fileWithError.getFileName().toString(), expectedError);

    // Upload the file
    uploadPage.uploadFileAndSubmit(fileWithError);

    // API 4xx upload failures render error summary on the same upload page.
    uploadPage.waitForUploadPage();

    // Verify error message is displayed
    String errorText = page.locator("#error-summary").textContent();
    assertThat(errorText)
        .as("Parse error should be visible for " + fieldName + "=" + value)
        .contains(expectedError);
  }

  // ==================== Helper Methods ====================

  private void stubClaimsApiUploadFailure(String fileName, String expectedError) {
    WIREMOCK.stubFor(
        post(urlPathEqualTo("/api/v1/bulk-submissions"))
            .atPriority(1)
            .withRequestBody(containing(fileName))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/problem+json")
                    .withBody("{\"detail\":\"" + expectedError + "\"}")));
  }

  private Path generateMalformedCsv(String fieldName, String value) throws IOException {
    // Keep a syntactically valid row and vary one field value per test case.
    // The API response is stubbed to simulate parser-level rejection for that upload.
    String csvContent = "OFFICE,account=0P322F\n"
        + "SCHEDULE,submissionPeriod=MAY-2026,areaOfLaw=LEGAL HELP,scheduleNum=0P322F/CIVIL\n"
        + "OUTCOME,FEE_CODE=CAPA," + fieldName + "=" + value + ","
        + "matterType=FAMX:FAPP,CASE_REF_NUMBER=TST/USR,CASE_START_DATE=15/05/2024,"
        + "CASE_ID=001,UFN=150524/001,PROCUREMENT_AREA=PA00120,ACCESS_POINT=AP00000,"
        + "CLIENT_FORENAME=Test,CLIENT_SURNAME=User,CLIENT_DATE_OF_BIRTH=02/01/1970,"
        + "UCN=02011970/T/USER,GENDER=M,ETHNICITY=12,DISABILITY=NCD,"
        + "CLIENT_POST_CODE=SW1A1AA,WORK_CONCLUDED_DATE=15/05/2024,CASE_STAGE_LEVEL=FPC01,"
        + "ADVICE_TIME=120,TRAVEL_TIME=0,WAITING_TIME=0,PROFIT_COST=100.00,"
        + "DISBURSEMENTS_AMOUNT=25.00,COUNSEL_COST=50.00,DISBURSEMENTS_VAT=5.00,"
        + "TRAVEL_WAITING_COSTS=0.00,VAT_INDICATOR=Y,LONDON_NONLONDON_RATE=N,"
        + "TRAVEL_COSTS=10.00,OUTCOME_CODE=FX,POSTAL_APPL_ACCP=Y,"
        + "NATIONAL_REF_MECHANISM_ADVICE=Y,LEGACY_CASE=N,"
        + "ADDITIONAL_TRAVEL_PAYMENT=N,ELIGIBLE_CLIENT_INDICATOR=Y,IRC_SURGERY=N,"
        + "SUBSTANTIVE_HEARING=N,TOLERANCE_INDICATOR=N,SURGERY_DATE=15/05/2024,"
        + "REP_ORDER_DATE=15/05/2024,TRANSFER_DATE=15/05/2024,SCHEDULE_REF=0P322F/2026/001\n";
    return writeFile(fieldName + "_parse_error_test.csv", csvContent);
  }
}

