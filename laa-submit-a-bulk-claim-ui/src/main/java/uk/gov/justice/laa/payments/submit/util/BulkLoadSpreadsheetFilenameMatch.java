package uk.gov.justice.laa.payments.submit.util;

public record BulkLoadSpreadsheetFilenameMatch(boolean matched, boolean isMac, String version) {

  public static final String NOT_APPLICABLE = "N/A";

  private static final BulkLoadSpreadsheetFilenameMatch NO_MATCH =
      new BulkLoadSpreadsheetFilenameMatch(false, false, NOT_APPLICABLE);

  public static BulkLoadSpreadsheetFilenameMatch noMatch() {
    return NO_MATCH;
  }
}
