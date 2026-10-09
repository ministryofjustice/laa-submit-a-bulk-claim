package uk.gov.justice.laa.payments.submit.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Bulk load spreadsheet filename util tests")
class BulkLoadSpreadsheetFilenameUtilTest {

  BulkLoadSpreadsheetFilenameUtil bulkLoadSpreadsheetFilenameUtil =
      new BulkLoadSpreadsheetFilenameUtil();

  @ParameterizedTest
  @CsvSource({
    "claims-v2.00.csv, 2.00",
    "claims-v3.14.csv, 3.14",
    "CLAIMS-V2.00.CSV, 2.00",
    "CivilBulkload0P322FJAN2026_v2.00.csv, 2.00"
  })
  @DisplayName("Should detect the standard LAA Bulk Load Spreadsheet naming convention")
  void shouldDetectStandardConvention(String filename, String expectedVersion) {
    // When
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    // Then
    assertThat(result.matched()).isTrue();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isEqualTo(expectedVersion);
  }

  @ParameterizedTest
  @CsvSource({
    "claims-mac-v1.42.csv, 1.42",
    "claims-MAC-V1.42.CSV, 1.42",
    "CivilBulkload0111EFJAN2026-mac-v9.01.csv, 9.01"
  })
  @DisplayName("Should detect the Mac-variant LAA Bulk Load Spreadsheet naming convention")
  void shouldDetectMacConvention(String filename, String expectedVersion) {
    // When
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    // Then
    assertThat(result.matched()).isTrue();
    assertThat(result.isMac()).isTrue();
    assertThat(result.version()).isEqualTo(expectedVersion);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "my-own-claims.csv",
        "claims.csv",
        "claims-v2.00.xlsx",
        "claims-v2.csv",
        "claims-v2.0.csv",
        "claims-v10.01.csv",
        "claims-v2.00.csv\n",
        "claims-mac-v1.42.csv\r\n"
      })
  @DisplayName("Should report non-matching filenames as not matched")
  void shouldReportNonMatchingFilenames(String filename) {
    // When
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    // Then
    assertThat(result.matched()).isFalse();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"claims.txt", "claims.xml", "claims-v2.00.txt", "claims-mac-v1.42.xml"})
  @DisplayName(
      "Should report .txt/.xml filenames as not matched, since the convention is .csv only")
  void shouldReportNonCsvFilenamesAsNotMatched(String filename) {
    // When
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    // Then
    assertThat(result.matched()).isFalse();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isNull();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should report null/empty filenames as not matched")
  void shouldReportNullOrEmptyFilenamesAsNotMatched(String filename) {
    // When
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    // Then
    assertThat(result.matched()).isFalse();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isNull();
  }

  @Test
  @DisplayName("Should expose a reusable not-matched instance")
  void shouldExposeNoMatchFactory() {
    // When
    BulkLoadSpreadsheetFilenameMatch result = BulkLoadSpreadsheetFilenameMatch.noMatch();
    // Then
    assertThat(result.matched()).isFalse();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isNull();
  }
}
