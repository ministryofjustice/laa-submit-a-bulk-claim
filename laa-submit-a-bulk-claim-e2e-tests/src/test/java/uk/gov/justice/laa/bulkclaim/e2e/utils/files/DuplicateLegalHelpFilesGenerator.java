package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates two Legal Help files for duplicate testing scenarios.
 * Handles both duplicate rejection and acceptance cases based on time gaps.
 * Direct Java equivalent to generateTwoLegalHelpDuplicateFiles.ts and generateTwoLegalHelpAcceptedFiles.ts
 */
public final class DuplicateLegalHelpFilesGenerator {

  private static final DateTimeFormatter SHORT_PERIOD =
      DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter SHORT_PERIOD_PARSER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("MMM-yyyy")
          .toFormatter(Locale.ENGLISH);
  private static final DateTimeFormatter SLASH_DATE = 
      DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK);

  private DuplicateLegalHelpFilesGenerator() {}

  /**
   * Result class for pair generation
   */
  public static class DuplicateFilePair {
    public final Path firstFile;
    public final Path secondFile;
    public final String office;

    public DuplicateFilePair(Path firstFile, Path secondFile, String office) {
      this.firstFile = firstFile;
      this.secondFile = secondFile;
      this.office = office;
    }
  }

  /**
   * Generate two files that will be rejected as duplicates (< 3 months apart).
   */
  public static DuplicateFilePair generateDuplicateRejectPair(
      String format,
      String office,
      Path firstPath,
      Path secondPath)
      throws Exception {
    
    // Get two submission periods less than 3 months apart
    // For simplicity, using consecutive months
    String period1 = "JAN-2025";
    String period2 = "FEB-2025"; // Only 1 month apart - will trigger duplicate rule
    
    DatesForPeriodPair dates = deriveValidDates(period1);
    
    // File 1: Valid dates
    Map<String, String> claim1 = new HashMap<>();
    claim1.put("caseStartDate", dates.caseStartDate);
    claim1.put("workConcludedDate", getCutoffDate(period1, period2).minusDays(2).format(SLASH_DATE));
    claim1.put("transferDate", dates.transferDate);
    claim1.put("surgeryDate", dates.surgeryDate);
    
    Path file1 = LegalHelpGenerator.generateFromClaimsTable(format, List.of(claim1), firstPath);
    
    // File 2: Dates that trigger duplicate cutoff
    DatesForPeriodPair dates2 = deriveValidDates(period2);
    Map<String, String> claim2 = new HashMap<>();
    claim2.put("caseStartDate", dates2.caseStartDate);
    claim2.put("workConcludedDate", getCutoffDate(period1, period2).minusDays(1).format(SLASH_DATE));
    claim2.put("transferDate", dates2.transferDate);
    claim2.put("surgeryDate", dates2.surgeryDate);
    
    Path file2 = LegalHelpGenerator.generateFromClaimsTable(format, List.of(claim2), secondPath);
    
    return new DuplicateFilePair(file1, file2, office);
  }

  /**
   * Generate two files that will be accepted (>= 3 months apart).
   */
  public static DuplicateFilePair generateDuplicateAcceptPair(
      String format,
      String office,
      Path firstPath,
      Path secondPath)
      throws Exception {
    
    // Get two submission periods at least 3 months apart
    String period1 = "JAN-2025";
    String period2 = "JUN-2025"; // 5 months apart - will pass duplicate rule
    
    DatesForPeriodPair dates = deriveValidDates(period1);
    LocalDate cutoffDate = getCutoffDate(period1, period2);
    
    // File 1: Valid dates
    Map<String, String> claim1 = new HashMap<>();
    claim1.put("caseStartDate", dates.caseStartDate);
    claim1.put("workConcludedDate", cutoffDate.format(SLASH_DATE));
    claim1.put("transferDate", dates.transferDate);
    claim1.put("surgeryDate", dates.surgeryDate);
    
    Path file1 = LegalHelpGenerator.generateFromClaimsTable(format, List.of(claim1), firstPath);
    
    // File 2: Dates on or after cutoff (will be accepted)
    DatesForPeriodPair dates2 = deriveValidDates(period2);
    Map<String, String> claim2 = new HashMap<>();
    claim2.put("caseStartDate", dates2.caseStartDate);
    claim2.put("workConcludedDate", cutoffDate.format(SLASH_DATE));
    claim2.put("transferDate", dates2.transferDate);
    claim2.put("surgeryDate", dates2.surgeryDate);
    
    Path file2 = LegalHelpGenerator.generateFromClaimsTable(format, List.of(claim2), secondPath);
    
    return new DuplicateFilePair(file1, file2, office);
  }

  // ==================== Utility Methods ====================

  private static LocalDate periodToLocalDate(String period) {
    YearMonth yearMonth = YearMonth.parse(period, SHORT_PERIOD_PARSER);
    return yearMonth.atDay(1);
  }

  /**
   * Duplicate cutoff rule:
   * AnchorPeriod = max(period1, period2)
   * CutoffPeriod = AnchorPeriod - 3 months
   * CutoffDate = 20th of following month
   */
  private static LocalDate getCutoffDate(String period1, String period2) {
    LocalDate date1 = periodToLocalDate(period1);
    LocalDate date2 = periodToLocalDate(period2);
    LocalDate anchorDate = date1.isAfter(date2) ? date1 : date2;
    
    LocalDate cutoffPeriod = anchorDate.minusMonths(3);
    return cutoffPeriod.plusMonths(1).withDayOfMonth(20);
  }

  /**
   * Derive valid dates for a given period.
   * CaseStart = period - 3 months
   * WorkConcluded = CaseStart + 1 day
   */
  private static DatesForPeriodPair deriveValidDates(String period) {
    LocalDate periodDate = periodToLocalDate(period);
    LocalDate caseStart = periodDate.minusMonths(3);
    LocalDate nextDay = caseStart.plusDays(1);
    
    return new DatesForPeriodPair(
        caseStart.format(SLASH_DATE),
        nextDay.format(SLASH_DATE),
        nextDay.format(SLASH_DATE),
        nextDay.format(SLASH_DATE)
    );
  }

  // ==================== Data Class ====================

  private static class DatesForPeriodPair {
    String caseStartDate;
    String workConcludedDate;
    String transferDate;
    String surgeryDate;

    DatesForPeriodPair(String caseStartDate, String workConcludedDate, 
                       String transferDate, String surgeryDate) {
      this.caseStartDate = caseStartDate;
      this.workConcludedDate = workConcludedDate;
      this.transferDate = transferDate;
      this.surgeryDate = surgeryDate;
    }
  }
}

