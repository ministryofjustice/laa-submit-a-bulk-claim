package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;

/** Domain generator facade for Legal help (civil) payloads. */
public final class CivilFileGenerator {

  private static final DateTimeFormatter SHORT_PERIOD_PARSER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("MMM-yyyy")
          .toFormatter(Locale.ENGLISH);

  private CivilFileGenerator() {}

  public static Path generateMinimalSubmissionFile(String format, int outcomes, Path filePath)
      throws IOException {
    return FileGeneratorUtil.generateMinimalSubmissionFile("Legal help", format, outcomes, filePath);
  }

  public static Path generateFromClaimsTable(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    return FileGeneratorUtil.generateFromClaimsTable("Legal help", format, claimRows, filePath);
  }

  public static FileGeneratorUtil.GeneratedFilePair generateTwoLegalHelpFilesMonthsApart(
      String format,
      String office,
      int monthsDifference,
      Map<String, String> claimRow,
      Path firstFilePath,
      Path secondFilePath)
      throws IOException {
    SubmissionPeriodHelper.PeriodRangeResult periods =
        SubmissionPeriodHelper.findTwoValidPeriodsApart(office, "LEGAL HELP", Math.max(0, monthsDifference));

    Map<String, String> source = claimRow == null ? Map.of() : claimRow;
    String ucn = source.getOrDefault("ucn", "01011990/S/AUT01");
    String ufn = source.getOrDefault("ufn", "010625/001");
    String feeCode1 = source.getOrDefault("feeCode1", source.getOrDefault("feeCode", "ICISD"));
    String feeCode2 = source.getOrDefault("feeCode2", source.getOrDefault("feeCode", feeCode1));

    DateRange base1 = deriveValidDates(periods.period1());
    DateRange base2 = deriveValidDates(periods.period2());
    CcdPair ccdPair =
        monthsDifference > 2
            ? new CcdPair(base1.defaultConcludedDate(), base2.defaultConcludedDate())
            : deriveDuplicateRejectDates(periods.period1(), periods.period2());

    Map<String, String> first = new HashMap<>();
    first.put("ucn", ucn);
    first.put("ufn", ufn);
    first.put("feeCode", feeCode1);
    first.put("office", periods.officeUsed());
    first.put("submissionPeriod", periods.period1());
    first.put("startDate", base1.caseStartDate());
    first.put("workConcludedDate", ccdPair.firstCcd());
    first.put("transferDate", ccdPair.firstCcd());
    first.put("surgeryDate", ccdPair.firstCcd());

    Map<String, String> second = new HashMap<>();
    second.put("ucn", ucn);
    second.put("ufn", ufn);
    second.put("feeCode", feeCode2);
    second.put("office", periods.officeUsed());
    second.put("submissionPeriod", periods.period2());
    second.put("startDate", base2.caseStartDate());
    second.put("workConcludedDate", ccdPair.secondCcd());
    second.put("transferDate", ccdPair.secondCcd());
    second.put("surgeryDate", ccdPair.secondCcd());

    Path firstGenerated = generateFromClaimsTable(format, List.of(first), firstFilePath);
    Path secondGenerated = generateFromClaimsTable(format, List.of(second), secondFilePath);
    return new FileGeneratorUtil.GeneratedFilePair(firstGenerated, secondGenerated);
  }

  public static FileGeneratorUtil.GeneratedFilePair generateTwoLegalHelpFilesOutsideDuplicateCutoff(
      String format,
      String office,
      Map<String, String> claimRow,
      Path firstFilePath,
      Path secondFilePath)
      throws IOException {
    SubmissionPeriodHelper.PeriodRangeResult periods =
        SubmissionPeriodHelper.findTwoValidPeriodsApart(office, "LEGAL HELP", 2);

    Map<String, String> source = claimRow == null ? Map.of() : claimRow;
    String ucn = source.getOrDefault("ucn", "01011990/S/AUT01");
    String ufn = source.getOrDefault("ufn", "010625/001");
    String feeCode1 = source.getOrDefault("feeCode1", source.getOrDefault("feeCode", "ICISD"));
    String feeCode2 = source.getOrDefault("feeCode2", source.getOrDefault("feeCode", feeCode1));

    DateRange base1 = deriveValidDates(periods.period1());
    DateRange base2 = deriveValidDates(periods.period2());
    CcdPair ccdPair = deriveAcceptedDates(periods.period1(), periods.period2());

    Map<String, String> first = new HashMap<>();
    first.put("ucn", ucn);
    first.put("ufn", ufn);
    first.put("feeCode", feeCode1);
    first.put("office", periods.officeUsed());
    first.put("submissionPeriod", periods.period1());
    first.put("startDate", base1.caseStartDate());
    first.put("workConcludedDate", ccdPair.firstCcd());
    first.put("transferDate", ccdPair.firstCcd());
    first.put("surgeryDate", ccdPair.firstCcd());

    Map<String, String> second = new HashMap<>();
    second.put("ucn", ucn);
    second.put("ufn", ufn);
    second.put("feeCode", feeCode2);
    second.put("office", periods.officeUsed());
    second.put("submissionPeriod", periods.period2());
    second.put("startDate", base2.caseStartDate());
    second.put("workConcludedDate", ccdPair.secondCcd());
    second.put("transferDate", ccdPair.secondCcd());
    second.put("surgeryDate", ccdPair.secondCcd());

    Path firstGenerated = generateFromClaimsTable(format, List.of(first), firstFilePath);
    Path secondGenerated = generateFromClaimsTable(format, List.of(second), secondFilePath);
    return new FileGeneratorUtil.GeneratedFilePair(firstGenerated, secondGenerated);
  }

  public static Path generateLegalHelpImmigrationFile(
      String format,
      String office,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    List<Map<String, String>> normalizedRows = new ArrayList<>();
    for (Map<String, String> row : claimRows) {
      Map<String, String> normalized = new HashMap<>();
      if (row != null) {
        normalized.putAll(row);
      }
      normalized.put("office", office);
      normalized.remove("expectedTotal");
      normalizedRows.add(normalized);
    }
    return generateFromClaimsTable(format, normalizedRows, filePath);
  }

  private static LocalDate periodToDate(String period) {
    String sanitized = period == null ? "" : period.replaceAll("[^A-Za-z0-9-]", "").trim();
    // Ensure proper format: extract month and year, then format as "MMM-yyyy"
    String[] parts = sanitized.split("-");
    if (parts.length >= 2) {
      String month = parts[0].toUpperCase(Locale.UK);
      String year = parts[1];
      // Ensure month is properly formatted (3 uppercase letters)
      if (month.length() == 3) {
        // Convert to title case (e.g., "SEP" -> "Sep")
        String monthTitleCase = month.substring(0, 1).toUpperCase(Locale.UK) + month.substring(1).toLowerCase(Locale.UK);
        sanitized = monthTitleCase + "-" + year;
      }
    }
    try {
      YearMonth ym = YearMonth.parse(sanitized, SHORT_PERIOD_PARSER);
      return ym.atDay(1);
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to parse period: " + period + " (sanitized: " + sanitized + ")", e);
    }
  }

  private static String formatDate(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK));
  }

  private static DateRange deriveValidDates(String period) {
    LocalDate periodDate = periodToDate(period);
    LocalDate caseStart = periodDate.minusMonths(3);
    LocalDate concluded = caseStart.plusDays(1);
    return new DateRange(formatDate(caseStart), formatDate(concluded));
  }

  private static CcdPair deriveDuplicateRejectDates(String period1, String period2) {
    LocalDate cutoff = getDuplicateCutoffDate(period1, period2);
    return new CcdPair(formatDate(cutoff.plusDays(1)), formatDate(cutoff.plusDays(2)));
  }

  private static CcdPair deriveAcceptedDates(String period1, String period2) {
    LocalDate cutoff = getDuplicateCutoffDate(period1, period2);
    return new CcdPair(formatDate(cutoff), formatDate(cutoff.plusDays(1)));
  }

  private static LocalDate getDuplicateCutoffDate(String period1, String period2) {
    LocalDate p1 = periodToDate(period1);
    LocalDate p2 = periodToDate(period2);
    LocalDate anchor = p1.isAfter(p2) ? p1 : p2;
    LocalDate cutoffPeriod = anchor.minusMonths(3);
    return LocalDate.of(cutoffPeriod.getYear(), cutoffPeriod.getMonth(), 1)
        .plusMonths(1)
        .withDayOfMonth(20);
  }

  private record DateRange(String caseStartDate, String defaultConcludedDate) {}

  private record CcdPair(String firstCcd, String secondCcd) {}
}
