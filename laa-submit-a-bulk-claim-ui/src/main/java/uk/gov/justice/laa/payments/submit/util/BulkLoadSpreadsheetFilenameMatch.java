package uk.gov.justice.laa.payments.submit.util;

public record BulkLoadSpreadsheetFilenameMatch(boolean matched, boolean isMac, String version) {

  private static final BulkLoadSpreadsheetFilenameMatch NO_MATCH =
      new BulkLoadSpreadsheetFilenameMatch(false, false, null);

  public static BulkLoadSpreadsheetFilenameMatch noMatch() {
    return NO_MATCH;
  }
}
