package uk.gov.justice.laa.bulkclaim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.justice.laa.bulkclaim.ui.helpers.AbstractUiTest;
import uk.gov.justice.laa.bulkclaim.ui.pages.UploadPage;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;

@DisplayName("Feature: Bulk submission upload validation")
class UploadValidationUiTest extends AbstractUiTest {

  private UploadPage uploadPage;
  private static final String OVER_LIMIT_FILE_NAME = "largeFile.csv";
  private static final int OVER_LIMIT_FILE_SIZE_BYTES = 11 * 1024 * 1024;

  @BeforeEach
  void setUpPageObjects() {
    uploadPage = new UploadPage(page);
    uploadPage.open(appLandingUrl());
  }

  @BeforeEach
  void enableRealUploadValidation() {
    BulkImportFileValidator realValidator = new BulkImportFileValidator("10MB");
    doAnswer(
            invocation -> {
              realValidator.validate(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(bulkImportFileValidator)
        .validate(any(), any());
  }

  @Test
  @DisplayName("Scenario: Upload fails with an empty file")
  void uploadFailsWithEmptyFile() throws IOException {
    Path emptyFile = writeFile("emptyFile.csv", "");

    uploadPage.uploadFileAndSubmit(emptyFile);

    assertErrorMessageContains("The selected file is empty");
  }

  @Test
  @DisplayName("Scenario: Upload fails when no file is attached")
  void uploadFailsWhenNoFileAttached() {
    uploadPage.submitWithoutSelectingFile();
    assertErrorMessageContains("Select a file");
  }

  @Test
  @DisplayName("Scenario: Upload fails with an invalid file type")
  void uploadFailsWithInvalidFileType() throws IOException {
    Path invalidFile = writeFile("invalid.docx", "not-a-bulk-submission-file");
    uploadPage.uploadFileAndSubmit(invalidFile);
    assertErrorMessageContains("The selected file must be a valid CSV, XML or TXT file");
  }

  @ParameterizedTest(name = "accepts {0} when MIME type is {1}")
  @CsvSource(
      textBlock = """
      txt,text/plain
      csv,text/plain
      csv,text/csv
      xml,text/xml
      csv,application/vnd.ms-excel
      """)
  @DisplayName("Scenario Outline: Accept submission when format has supported MIME type")
  void uploadAcceptsSupportedMimeTypes(String format, String mimeType) {
    uploadPage.uploadFileAndSubmit("valid." + format, mimeType, sampleContentForFormat(format));
    uploadPage.waitForUploadBeingCheckedPage();
    assertTrue(page.url().contains("/upload-is-being-checked"));
  }

  @ParameterizedTest(name = "rejects {0} when MIME type is {1}")
  @CsvSource(
      textBlock = """
      txt,application/xml
      txt,text/xml
      txt,text/csv
      txt,application/csv
      txt,application/vnd.ms-excel
      txt,application/json
      txt,application/pdf
      csv,application/xml
      csv,text/xml
      csv,text/html
      csv,application/json
      xml,text/csv
      xml,text/plain
      xml,application/vnd.ms-excel
      xml,application/csv
      xml,application/json
      """)
  @DisplayName("Scenario Outline: Reject submission when format has unsupported MIME type")
  void uploadRejectsUnsupportedMimeTypes(String format, String mimeType) {
    uploadPage.uploadFileAndSubmit("invalid." + format, mimeType, sampleContentForFormat(format));
    assertErrorMessageContains("The selected file must be a valid CSV, XML or TXT file");
  }

  @Test
  @DisplayName("Scenario: Upload fails with a file larger than 10MB")
  void uploadFailsWithFileLargerThanTenMb() throws IOException {
    Path largeFile = createOverLimitFile();

    uploadPage.uploadFileAndSubmit(largeFile);

    assertErrorMessageContains("The file must be 10MB or smaller");
  }

  @Test
  @DisplayName("Scenario: Upload fails with restricted office access")
  void uploadFailsWithRestrictedOfficeAccess() throws IOException {
    Path forbiddenOfficeFile =
        writeFile("forbidden-office.csv", "header-1,header-2\nvalue-1,value-2\n");

    uploadPage.uploadFileAndSubmit(forbiddenOfficeFile);

    assertErrorMessageContains("You do not have access to this office");
  }

  private void assertErrorMessageContains(String expectedText) {
    uploadPage.waitForUploadPage();
    String errorSummaryText = page.locator("#error-summary").innerText();
    System.out.println("UI error summary: " + errorSummaryText);
    assertTrue(errorSummaryText.contains(expectedText), "Expected error message was not present");
  }

  private Path createOverLimitFile() throws IOException {
    Path file = Path.of("build", "tmp", "ui", OVER_LIMIT_FILE_NAME);
    Files.createDirectories(file.getParent());
    Files.write(file, new byte[OVER_LIMIT_FILE_SIZE_BYTES]);
    return file;
  }

  private String sampleContentForFormat(String format) {
    return switch (format.toLowerCase()) {
      case "xml" -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<submission xmlns=\"http://www.legalservices.gov.uk/sms/ActivityManagement/XMLSchema/\">\n"
          + "  <office account=\"0P322F\">\n"
          + "    <schedule submissionPeriod=\"APR-2026\" areaOfLaw=\"LEGAL HELP\" scheduleNum=\"0P322F/CIVIL\">\n"
          + "      <outcome matterType=\"FAMX:FAPP\">\n"
          + "        <outcomeItem name=\"FEE_CODE\">CAPA</outcomeItem>\n"
          + "      </outcome>\n"
          + "    </schedule>\n"
          + "  </office>\n"
          + "</submission>\n";
      case "txt", "csv" -> "OFFICE,account=0P322F\n"
          + "SCHEDULE,submissionPeriod=APR-2026,areaOfLaw=LEGAL HELP,scheduleNum=0P322F/CIVIL\n"
          + "OUTCOME,FEE_CODE=CAPA,matterType=FAMX:FAPP,CASE_REF_NUMBER=TST/USR,CASE_START_DATE=15/04/2024,"
          + "CASE_ID=001,UFN=150424/001,PROCUREMENT_AREA=PA00120,ACCESS_POINT=AP00000,CLIENT_FORENAME=Test,"
          + "CLIENT_SURNAME=User,CLIENT_DATE_OF_BIRTH=02/01/1970,UCN=02011970/T/USER,GENDER=M,ETHNICITY=12,"
          + "DISABILITY=NCD,CLIENT_POST_CODE=SW1A1AA,WORK_CONCLUDED_DATE=15/04/2024,CASE_STAGE_LEVEL=FPC01,"
          + "ADVICE_TIME=120,TRAVEL_TIME=0,WAITING_TIME=0,PROFIT_COST=100.00,DISBURSEMENTS_AMOUNT=25.00,"
          + "COUNSEL_COST=50.00,DISBURSEMENTS_VAT=5.00,TRAVEL_WAITING_COSTS=0.00,VAT_INDICATOR=Y,"
          + "LONDON_NONLONDON_RATE=N,TRAVEL_COSTS=10.00,OUTCOME_CODE=FX,POSTAL_APPL_ACCP=Y,"
          + "NATIONAL_REF_MECHANISM_ADVICE=Y,LEGACY_CASE=N,ADDITIONAL_TRAVEL_PAYMENT=N,"
          + "ELIGIBLE_CLIENT_INDICATOR=Y,IRC_SURGERY=N,SUBSTANTIVE_HEARING=N,TOLERANCE_INDICATOR=N,"
          + "SURGERY_DATE=15/04/2024,REP_ORDER_DATE=15/04/2024,TRANSFER_DATE=15/04/2024,SCHEDULE_REF=0P322F/2026/001\n";
      default -> throw new IllegalArgumentException("Unsupported test format: " + format);
    };
  }
}


