package uk.gov.justice.laa.payments.submit.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Detects whether an uploaded bulkload file matches the LAA Bulk Load Spreadsheet naming
 * convention.
 *
 * <p>A filename matches when it starts with {@code CivilBulkload}, {@code CrimeBulkload} or {@code
 * MediationBulkload} and is a {@code .csv} file (e.g. {@code
 * CivilBulkload0P322FJAN2026_v2.00.csv}). The remainder (office code, month/year) is not validated.
 * Additionally, a trailing {@code vN.NN.csv} version is captured, and a {@code -mac-vN.NN.csv} /
 * {@code _mac-vN.NN.csv} suffix flags the Mac variant. Both are optional extras and do not affect
 * whether the file matched.
 */
@Component
public class BulkLoadSpreadsheetFilenameUtil {

  private static final Pattern PREFIX_PATTERN =
      Pattern.compile("(?:Civil|Crime|Mediation)Bulkload.*\\.csv\\z", Pattern.CASE_INSENSITIVE);

  private static final Pattern MAC_PATTERN =
      Pattern.compile("mac-v(\\d\\.\\d{2})\\.csv\\z", Pattern.CASE_INSENSITIVE);

  private static final Pattern STANDARD_PATTERN =
      Pattern.compile("v(\\d\\.\\d{2})\\.csv\\z", Pattern.CASE_INSENSITIVE);

  public BulkLoadSpreadsheetFilenameMatch detect(String filename) {
    if (filename == null || filename.isBlank() || !PREFIX_PATTERN.matcher(filename).matches()) {
      return BulkLoadSpreadsheetFilenameMatch.noMatch();
    }

    Matcher macMatcher = MAC_PATTERN.matcher(filename);
    if (macMatcher.find()) {
      return new BulkLoadSpreadsheetFilenameMatch(true, true, macMatcher.group(1));
    }

    Matcher standardMatcher = STANDARD_PATTERN.matcher(filename);
    if (standardMatcher.find()) {
      return new BulkLoadSpreadsheetFilenameMatch(true, false, standardMatcher.group(1));
    }

    return new BulkLoadSpreadsheetFilenameMatch(true, false, null);
  }
}
