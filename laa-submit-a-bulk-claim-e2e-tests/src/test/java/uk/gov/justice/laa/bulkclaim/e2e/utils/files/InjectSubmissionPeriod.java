package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Injects or modifies the submission period in an existing file.
 * Updates the SCHEDULE line with a new submission period.
 * Direct Java equivalent to injectSubmissionPeriod.ts
 */
public final class InjectSubmissionPeriod {

  private static final DateTimeFormatter PERIOD_FORMAT =
      DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);

  private InjectSubmissionPeriod() {}

  /**
   * Inject a new submission period into an existing file.
   * Modifies the SCHEDULE line to update submissionPeriod value.
   */
  public static Path injectPeriod(
      Path filePath,
      String newPeriod)
      throws IOException {
    
    List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
    List<String> modified = new java.util.ArrayList<>();
    
    for (String line : lines) {
      if (line.startsWith("SCHEDULE,")) {
        // Replace submissionPeriod value
        line = line.replaceAll("submissionPeriod=[^,]+", "submissionPeriod=" + newPeriod);
      }
      modified.add(line);
    }
    
    Files.write(filePath, modified, StandardCharsets.UTF_8);
    return filePath;
  }

  /**
   * Inject period relative to current month (e.g., "+1" for next month, "-1" for previous).
   */
  public static Path injectRelativePeriod(
      Path filePath,
      int monthOffset)
      throws IOException {
    
    YearMonth period = YearMonth.now().plusMonths(monthOffset);
    String periodStr = period.format(PERIOD_FORMAT).toUpperCase();
    
    return injectPeriod(filePath, periodStr);
  }
}

