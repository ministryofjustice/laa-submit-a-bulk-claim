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
    "CivilBulkload0P322FJAN2026_v2.00.csv, 2.00",
    "CRIMEBULKLOAD0P322FJAN2026_V3.14.CSV, 3.14"
  })
  @DisplayName("Should match the standard convention and capture the version")
  void shouldDetectStandardConvention(String filename, String expectedVersion) {
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    assertThat(result.matched()).isTrue();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isEqualTo(expectedVersion);
  }

  @ParameterizedTest
  @CsvSource({
    "MediationBulkload0P322FJAN2026_mac-v2.00.csv, 2.00",
    "CivilBulkload0111EFJAN2026-mac-v9.01.csv, 9.01",
    "CivilBulkload0111EFJAN2026_MAC-V1.42.CSV, 1.42"
  })
  @DisplayName("Should match the Mac variant and capture the version")
  void shouldDetectMacConvention(String filename, String expectedVersion) {
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    assertThat(result.matched()).isTrue();
    assertThat(result.isMac()).isTrue();
    assertThat(result.version()).isEqualTo(expectedVersion);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "CrimeBulkload0P322FJAN2026.csv",
        "CivilBulkload.csv",
        "CivilBulkload0P322FJAN2026_v10.01.csv"
      })
  @DisplayName("Should match on the filename prefix alone, with no version or Mac flag")
  void shouldMatchWithoutVersion(String filename) {
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    assertThat(result.matched()).isTrue();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isEqualTo("N/A");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "my-own-claims.csv",
        "claims-v2.00.csv",
        "claims-mac-v1.42.csv",
        "XCivilBulkload0P322FJAN2026.csv",
        "CivilBulkload0P322FJAN2026.xlsx",
        "CivilBulkload0P322FJAN2026.txt",
        "CivilBulkload0P322FJAN2026_v2.00.csv\n"
      })
  @DisplayName("Should report non-matching filenames as not matched")
  void shouldReportNonMatchingFilenames(String filename) {
    BulkLoadSpreadsheetFilenameMatch result = bulkLoadSpreadsheetFilenameUtil.detect(filename);
    assertThat(result.matched()).isFalse();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isEqualTo("N/A");
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
    assertThat(result.version()).isEqualTo("N/A");
  }

  @Test
  @DisplayName("Should expose a reusable not-matched instance")
  void shouldExposeNoMatchFactory() {
    // When
    BulkLoadSpreadsheetFilenameMatch result = BulkLoadSpreadsheetFilenameMatch.noMatch();
    // Then
    assertThat(result.matched()).isFalse();
    assertThat(result.isMac()).isFalse();
    assertThat(result.version()).isEqualTo("N/A");
  }
}
