package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;

/** Utility methods equivalent to TS filegenerator.ts helpers. */
public final class FileGeneratorUtil {

  private static final List<String> INVALID_EXTENSIONS = List.of(".pdf", ".exe", ".jpg", ".png", ".docx");
   private static final DateTimeFormatter SHORT_PERIOD = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
   private static final DateTimeFormatter SHORT_PERIOD_PARSER =
       new DateTimeFormatterBuilder()
           .parseCaseInsensitive()
           .appendPattern("MMM-yyyy")
           .toFormatter(Locale.ENGLISH);
  private static final DateTimeFormatter SLASH_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK);
  private static final String XML_SCHEMA_LOCATION =
      "http://www.legalservices.gov.uk/sms/ActivityManagement/XMLSchema/LSCSMSBulkLoadSchemaV3.xsd";

  private static final List<String> LEGAL_HELP_ORDER =
      List.of(
          "FEE_CODE",
          "CASE_REF_NUMBER",
          "CASE_START_DATE",
          "CASE_ID",
          "UFN",
          "PROCUREMENT_AREA",
          "ACCESS_POINT",
          "CLIENT_FORENAME",
          "CLIENT_SURNAME",
          "CLIENT_DATE_OF_BIRTH",
          "UCN",
          "GENDER",
          "ETHNICITY",
          "DISABILITY",
          "CLIENT_POST_CODE",
          "WORK_CONCLUDED_DATE",
          "CASE_STAGE_LEVEL",
          "ADVICE_TIME",
          "TRAVEL_TIME",
          "WAITING_TIME",
          "PROFIT_COST",
          "DISBURSEMENTS_AMOUNT",
          "COUNSEL_COST",
          "DISBURSEMENTS_VAT",
          "TRAVEL_WAITING_COSTS",
          "VAT_INDICATOR",
          "LONDON_NONLONDON_RATE",
          "TRAVEL_COSTS",
          "OUTCOME_CODE",
          "POSTAL_APPL_ACCP",
          "NATIONAL_REF_MECHANISM_ADVICE",
          "LEGACY_CASE",
          "ADDITIONAL_TRAVEL_PAYMENT",
          "ELIGIBLE_CLIENT_INDICATOR",
          "IRC_SURGERY",
          "SUBSTANTIVE_HEARING",
          "TOLERANCE_INDICATOR",
          "SURGERY_DATE",
          "REP_ORDER_DATE",
          "TRANSFER_DATE",
          "SCHEDULE_REF",
          "PRIOR_AUTHORITY_REF",
          "JR_FORM_FILLING",
          "DETENTION_TRAVEL_WAITING_COSTS",
          "HO_INTERVIEW",
          "ADJOURNED_HEARING_FEE",
          "CMRH_ORAL",
          "CMRH_TELEPHONE");

  private static final List<String> CRIME_ORDER =
      List.of(
          "FEE_CODE",
          "UFN",
          "CLIENT_FORENAME",
          "CLIENT_SURNAME",
          "CLIENT_DATE_OF_BIRTH",
          "GENDER",
          "ETHNICITY",
          "DISABILITY",
          "CASE_START_DATE",
          "PROFIT_COST",
          "DISBURSEMENTS_AMOUNT",
          "DISBURSEMENTS_VAT",
          "VAT_INDICATOR",
          "TRAVEL_COSTS",
          "OUTCOME_CODE",
          "CRIME_MATTER_TYPE",
          "TRAVEL_WAITING_COSTS",
          "WORK_CONCLUDED_DATE",
          "NO_OF_SUSPECTS",
          "NO_OF_POLICE_STATION",
          "POLICE_STATION",
          "DUTY_SOLICITOR",
          "YOUTH_COURT",
          "SCHEME_ID",
          "DSCC_NUMBER",
          "POSTAL_APPL_ACCP",
          "NATIONAL_REF_MECHANISM_ADVICE",
          "LEGACY_CASE",
          "LONDON_NONLONDON_RATE",
          "ADDITIONAL_TRAVEL_PAYMENT",
          "ELIGIBLE_CLIENT_INDICATOR",
          "IRC_SURGERY",
          "SUBSTANTIVE_HEARING",
          "TOLERANCE_INDICATOR",
          "REP_ORDER_DATE",
          "TRANSFER_DATE",
          "SURGERY_DATE",
          "CLIENT_LEGALLY_AIDED");

  private static final List<String> MEDIATION_ORDER =
      List.of(
          "FEE_CODE",
          "CASE_START_DATE",
          "CASE_ID",
          "UFN",
          "CLIENT_FORENAME",
          "CLIENT_SURNAME",
          "CLIENT_DATE_OF_BIRTH",
          "UCN",
          "GENDER",
          "ETHNICITY",
          "DISABILITY",
          "CLIENT_POST_CODE",
          "CLIENT_LEGALLY_AIDED",
          "CLIENT2_FORENAME",
          "CLIENT2_SURNAME",
          "CLIENT2_DATE_OF_BIRTH",
          "CLIENT2_UCN",
          "CLIENT2_GENDER",
          "CLIENT2_ETHNICITY",
          "CLIENT2_DISABILITY",
          "CLIENT2_POST_CODE",
          "CLIENT2_LEGALLY_AIDED",
          "MED_CONCLUDED_DATE",
          "WORK_CONCLUDED_DATE",
          "NUMBER_OF_MEDIATION_SESSIONS",
          "MEDIATION_TIME",
          "CASE_REF_NUMBER",
          "OUTCOME_CODE",
          "DISBURSEMENTS_AMOUNT",
          "DISBURSEMENTS_VAT",
          "VAT_INDICATOR",
          "UNIQUE_CASE_ID",
          "OUTREACH",
          "REFERRAL",
          "POSTAL_APPL_ACCP",
          "CLIENT2_POSTAL_APPL_ACCP",
          "SCHEDULE_REF",
          "NATIONAL_REF_MECHANISM_ADVICE",
          "LEGACY_CASE",
          "LONDON_NONLONDON_RATE",
          "ADDITIONAL_TRAVEL_PAYMENT",
          "ELIGIBLE_CLIENT_INDICATOR",
          "IRC_SURGERY",
          "SUBSTANTIVE_HEARING",
          "TOLERANCE_INDICATOR",
          "DUTY_SOLICITOR",
          "YOUTH_COURT");

  private FileGeneratorUtil() {}

  public static Path generateEmptyFile(Path filePath) throws IOException {
    ensureDirExists(filePath.getParent());
    Files.writeString(filePath, "", StandardCharsets.UTF_8);
    waitForFile(filePath, 5000);
    return filePath;
  }

  public static Path generateInvalidFile(Path filePath) throws IOException {
    String ext = INVALID_EXTENSIONS.get((int) (Math.random() * INVALID_EXTENSIONS.size()));
    Path target = filePath.toString().contains(".") ? filePath : Path.of(filePath + ext);
    ensureDirExists(target.getParent());
    Files.writeString(target, "This is not a valid file format for upload.", StandardCharsets.UTF_8);
    waitForFile(target, 5000);
    return target;
  }

  public static Path generateLargeFile(Path filePath, int sizeInMb) throws IOException {
    ensureDirExists(filePath.getParent());
    byte[] chunk = "A".repeat(1024 * 1024).getBytes(StandardCharsets.UTF_8);
    try (OutputStream os = Files.newOutputStream(filePath)) {
      for (int i = 0; i < sizeInMb; i++) {
        os.write(chunk);
      }
    }
    waitForFile(filePath, 15000);
    return filePath;
  }

  public static Path generateMinimalSubmissionFile(String areaOfLaw, String format, int outcomes, Path filePath)
      throws IOException {
    ensureDirExists(filePath.getParent());
    String normalizedFormat = format.toLowerCase(Locale.ROOT);
    GenerationContext context = resolveContext(areaOfLaw, List.of());
    List<Map<String, String>> rows = buildRows(areaOfLaw, context, Math.max(0, outcomes), List.of());

    String content =
        "xml".equals(normalizedFormat)
            ? buildXmlSubmission(areaOfLaw, context, rows)
            : buildRecordDelimited(areaOfLaw, context, rows);

    Files.writeString(filePath, content, StandardCharsets.UTF_8);
    waitForFile(filePath, 5000);
    return filePath;
  }

  public static Path generateFromClaimsTable(
      String areaOfLaw,
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    ensureDirExists(filePath.getParent());

    List<Map<String, String>> safeRows = claimRows == null ? List.of() : claimRows;
    GenerationContext context = resolveContext(areaOfLaw, safeRows);
    List<Map<String, String>> rows = buildRows(areaOfLaw, context, safeRows.size(), safeRows);

    String normalizedFormat = format.toLowerCase(Locale.ROOT);
    String content =
        "xml".equals(normalizedFormat)
            ? buildXmlSubmission(areaOfLaw, context, rows)
            : buildRecordDelimited(areaOfLaw, context, rows);

    Files.writeString(filePath, content, StandardCharsets.UTF_8);
    waitForFile(filePath, 5000);
    return filePath;
  }

  public static Path duplicateLastRow(Path csvPath) throws IOException {
    List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
    for (int i = lines.size() - 1; i >= 0; i--) {
      String line = lines.get(i);
      if (line != null && line.startsWith("OUTCOME,")) {
        lines.add(line);
        Files.write(csvPath, lines, StandardCharsets.UTF_8);
        return csvPath;
      }
    }
    return csvPath;
  }

  public static Path overrideField(Path csvPath, String field, String value) throws IOException {
    List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
    if (lines.isEmpty()) {
      return csvPath;
    }

    String normalizedField = normalizeFieldAlias(field);
    boolean isScheduleField =
        "SUBMISSIONPERIOD".equals(normalizedField)
            || "AREAOFLAW".equals(normalizedField)
            || "SCHEDULENUM".equals(normalizedField);
    boolean isOfficeField = "OFFICE".equals(normalizedField) || "ACCOUNT".equals(normalizedField);

    String scheduleKey = canonicalScheduleKey(field);
    String outcomeKey = canonicalOutcomeKey(field);
    List<String> rewritten = new ArrayList<>();

    for (String line : lines) {
      if (line == null || line.isBlank()) {
        rewritten.add(line);
      } else if (line.startsWith("SCHEDULE,")) {
        rewritten.add(isScheduleField ? replaceOrAppendToken(line, scheduleKey, value) : line);
      } else if (line.startsWith("OFFICE,") && isOfficeField) {
        rewritten.add(replaceOrAppendToken(line, "account", value));
      } else if (line.startsWith("OUTCOME,")) {
        rewritten.add((isScheduleField || isOfficeField) ? line : replaceOrAppendToken(line, outcomeKey, value));
      } else {
        rewritten.add(line);
      }
    }

    Files.write(csvPath, rewritten, StandardCharsets.UTF_8);
    return csvPath;
  }

  public static Path updateLastRowFields(Path csvPath, Map<String, String> fields) throws IOException {
    List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);

    int lastOutcomeIndex = -1;
    for (int i = lines.size() - 1; i >= 0; i--) {
      String line = lines.get(i);
      if (line != null && line.startsWith("OUTCOME,")) {
        lastOutcomeIndex = i;
        break;
      }
    }
    if (lastOutcomeIndex < 0) {
      return csvPath;
    }

    String rewritten = lines.get(lastOutcomeIndex);
    for (Map.Entry<String, String> entry : fields.entrySet()) {
      rewritten = replaceOrAppendToken(rewritten, canonicalOutcomeKey(entry.getKey()), entry.getValue());
    }

    lines.set(lastOutcomeIndex, rewritten);
    Files.write(csvPath, lines, StandardCharsets.UTF_8);
    return csvPath;
  }

  public record GeneratedFilePair(Path firstFile, Path secondFile) {}

  public static Path generateLegalHelpImmigrationFile(
      String format,
      String office,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    return CivilFileGenerator.generateLegalHelpImmigrationFile(format, office, claimRows, filePath);
  }

  public static GeneratedFilePair generateTwoLegalHelpFilesMonthsApart(
      String format,
      String office,
      int monthsDifference,
      Map<String, String> claimRow,
      Path firstFilePath,
      Path secondFilePath)
      throws IOException {
    return CivilFileGenerator.generateTwoLegalHelpFilesMonthsApart(
        format, office, monthsDifference, claimRow, firstFilePath, secondFilePath);
  }

  public static GeneratedFilePair generateTwoLegalHelpFilesOutsideDuplicateCutoff(
      String format,
      String office,
      Map<String, String> claimRow,
      Path firstFilePath,
      Path secondFilePath)
      throws IOException {
    return CivilFileGenerator.generateTwoLegalHelpFilesOutsideDuplicateCutoff(
        format, office, claimRow, firstFilePath, secondFilePath);
  }

  private static List<Map<String, String>> buildRows(
      String areaOfLaw,
      GenerationContext context,
      int count,
      List<Map<String, String>> claimRows) {
    List<Map<String, String>> rows = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Map<String, String> row = defaultRow(areaOfLaw, context, i + 1);
      if (i < claimRows.size()) {
        row.putAll(normalizeFeatureFields(claimRows.get(i)));
      }
      row.put("office", row.getOrDefault("office", context.office()));
      row.put("submissionPeriod", row.getOrDefault("submissionPeriod", context.period()));
      row.put("areaOfLaw", row.getOrDefault("areaOfLaw", normalizeAreaOfLaw(areaOfLaw)));
      row.put("scheduleNum", row.getOrDefault("scheduleNum", context.scheduleNum()));
      row.put("matterType", row.getOrDefault("matterType", defaultMatterType(areaOfLaw, row.get("FEE_CODE"))));
      if (row.containsKey("UFN") && !row.containsKey("UNIQUE_CASE_ID") && "Mediation".equalsIgnoreCase(normalizeAreaOfLaw(areaOfLaw))) {
        row.put("UNIQUE_CASE_ID", row.get("UFN"));
      }
      rows.add(row);
    }
    return rows;
  }

  private static GenerationContext resolveContext(String areaOfLaw, List<Map<String, String>> claimRows) {
    String normalizedArea = normalizeAreaOfLaw(areaOfLaw);
    String office = firstNonBlank(claimRows, List.of("office", "account"), defaultOffice());
    String feeCode = firstNonBlank(claimRows, List.of("feeCode", "FEE_CODE"), defaultFeeCode(normalizedArea));
    String explicitPeriod = normalizeSubmissionPeriod(firstNonBlank(claimRows, List.of("submissionPeriod"), null));
    String explicitScheduleNum = firstNonBlank(claimRows, List.of("scheduleNum"), null);

    // If any row uses "later" as workConcludedDate, we need a period old enough that
    // the 21st of its following month is already in the past (avoids "future date" error).
    boolean needsOldPeriodForLater = claimRows.stream()
        .anyMatch(r -> "later".equalsIgnoreCase(r.getOrDefault("workConcludedDate", ""))
            || "later".equalsIgnoreCase(r.getOrDefault("WORK_CONCLUDED_DATE", "")));

    if (explicitPeriod != null) {
      return new GenerationContext(
          office,
          explicitPeriod,
          null,
          null,
          explicitScheduleNum != null ? explicitScheduleNum : scheduleNumForArea(normalizedArea, office, explicitPeriod));
    }

    try {
      // Keep period selection aligned with the fee code category where applicable.
      String feeCodeForValidation =
          ("LEGAL HELP".equals(normalizedArea) || "CRIME LOWER".equals(normalizedArea))
              ? feeCode
              : null;

      // For "later" scenarios, keep finding periods until the 21st of the following month is in the past
      LocalDate today = LocalDate.now();
      SubmissionPeriodHelper.SubmissionPeriodResult result = null;
      Exception lastError = null;
      int maxAttempts = needsOldPeriodForLater ? 30 : 3;
      for (int attempt = 0; attempt < maxAttempts; attempt++) {
        try {
          SubmissionPeriodHelper.SubmissionPeriodResult candidate =
              SubmissionPeriodHelper.getUniqueSubmissionPeriod(
                  office, normalizedArea, feeCodeForValidation);
          if (needsOldPeriodForLater) {
            YearMonth periodYM = parseSubmissionPeriod(normalizeSubmissionPeriod(candidate.period()));
            if (periodYM.plusMonths(1).atDay(21).isAfter(today)) {
              // Period too recent for "later" — keep going to find an older one
              if (attempt < maxAttempts - 1) {
                try { Thread.sleep(100); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
              }
              continue;
            }
          }
          result = candidate;
          break;
        } catch (Exception e) {
          lastError = e;
          if (attempt < maxAttempts - 1) {
            try {
              Thread.sleep(200L * Math.min(attempt + 1, 3));
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        }
      }

      if (result == null) {
        throw new IllegalStateException("Unable to resolve submission period", lastError);
      }

      String period = normalizeSubmissionPeriod(result.period());
      return new GenerationContext(
          office,
          period,
          result.scheduleStart(),
          result.scheduleEnd(),
          explicitScheduleNum != null ? explicitScheduleNum : scheduleNumForArea(normalizedArea, office, period));
    } catch (Exception ignored) {
      String fallbackPeriod = defaultSubmissionPeriod();
      return new GenerationContext(
          office,
          fallbackPeriod,
          null,
          null,
          explicitScheduleNum != null ? explicitScheduleNum : scheduleNumForArea(normalizedArea, office, fallbackPeriod));
    }
  }

  private static LinkedHashMap<String, String> defaultRow(String areaOfLaw, GenerationContext context, int index) {
    String normalizedArea = normalizeAreaOfLaw(areaOfLaw);
    OutcomeDates dates = deriveOutcomeDates(context);
    return switch (normalizedArea.toUpperCase(Locale.ROOT)) {
      case "CRIME LOWER", "CRIME" -> buildCrimeRow(context, dates, index);
      case "MEDIATION" -> buildMediationRow(context, dates, index);
      default -> buildLegalHelpRow(context, dates, index);
    };
  }

  private static LinkedHashMap<String, String> buildLegalHelpRow(
      GenerationContext context, OutcomeDates dates, int index) {
    LinkedHashMap<String, String> row = new LinkedHashMap<>();
    row.put("office", context.office());
    row.put("submissionPeriod", context.period());
    row.put("areaOfLaw", "Legal help");
    row.put("scheduleNum", context.scheduleNum());
    row.put("matterType", "FAMX:FAPP");
    row.put("FEE_CODE", "CAPA");
    row.put("CASE_REF_NUMBER", "CASE-" + String.format(Locale.ROOT, "%03d", index));
    row.put("CASE_START_DATE", formatDate(dates.caseStartDate()));
    row.put("CASE_ID", String.format(Locale.ROOT, "%03d", index));
    row.put("UFN", buildUfn(dates.caseStartDate(), index));
    row.put("PROCUREMENT_AREA", "PA00120");
    row.put("ACCESS_POINT", "AP00000");
    row.put("CLIENT_FORENAME", "Test");
    row.put("CLIENT_SURNAME", "User" + index);
    row.put("CLIENT_DATE_OF_BIRTH", "01/01/1990");
    row.put("UCN", String.format(Locale.ROOT, "01011990/T/USR%02d", index));
    row.put("GENDER", "M");
    row.put("ETHNICITY", "12");
    row.put("DISABILITY", "NCD");
    row.put("CLIENT_POST_CODE", "SE1 1AA");
    row.put("WORK_CONCLUDED_DATE", formatDate(dates.workConcludedDate()));
    row.put("CASE_STAGE_LEVEL", "FPC01");
    row.put("ADVICE_TIME", "120");
    row.put("TRAVEL_TIME", "0");
    row.put("WAITING_TIME", "0");
    row.put("PROFIT_COST", "120.00");
    row.put("DISBURSEMENTS_AMOUNT", "2.00");
    row.put("COUNSEL_COST", "19.33");
    row.put("DISBURSEMENTS_VAT", "1.00");
    row.put("TRAVEL_WAITING_COSTS", "0.00");
    row.put("VAT_INDICATOR", "Y");
    row.put("LONDON_NONLONDON_RATE", "N");
    row.put("TRAVEL_COSTS", "5.86");
    row.put("OUTCOME_CODE", "FX");
    row.put("POSTAL_APPL_ACCP", "Y");
    row.put("NATIONAL_REF_MECHANISM_ADVICE", "Y");
    row.put("LEGACY_CASE", "N");
    row.put("ADDITIONAL_TRAVEL_PAYMENT", "N");
    row.put("ELIGIBLE_CLIENT_INDICATOR", "Y");
    row.put("IRC_SURGERY", "N");
    row.put("SUBSTANTIVE_HEARING", "N");
    row.put("TOLERANCE_INDICATOR", "N");
    row.put("SURGERY_DATE", formatDate(dates.workConcludedDate()));
    row.put("REP_ORDER_DATE", formatDate(dates.repOrderDate()));
    row.put("TRANSFER_DATE", formatDate(dates.workConcludedDate()));
    row.put("SCHEDULE_REF", SubmissionPeriodHelper.generateScheduleRef(context.office()));
    return row;
  }

  private static LinkedHashMap<String, String> buildCrimeRow(
      GenerationContext context, OutcomeDates dates, int index) {
    LinkedHashMap<String, String> row = new LinkedHashMap<>();
    row.put("office", context.office());
    row.put("submissionPeriod", context.period());
    row.put("areaOfLaw", "Crime lower");
    row.put("scheduleNum", context.scheduleNum());
    row.put("matterType", "APPA");
    row.put("FEE_CODE", "APPA");
    row.put("UFN", buildUfn(dates.caseStartDate(), index));
    row.put("CLIENT_FORENAME", "Crime");
    row.put("CLIENT_SURNAME", "User" + index);
    row.put("CLIENT_DATE_OF_BIRTH", "01/01/1990");
    row.put("GENDER", "M");
    row.put("ETHNICITY", "99");
    row.put("DISABILITY", "NCD");
    row.put("CASE_START_DATE", formatDate(dates.caseStartDate()));
    row.put("PROFIT_COST", "40.00");
    row.put("DISBURSEMENTS_AMOUNT", "2.00");
    row.put("DISBURSEMENTS_VAT", "1.00");
    row.put("VAT_INDICATOR", "N");
    row.put("TRAVEL_COSTS", "10.00");
    row.put("OUTCOME_CODE", "CN04");
    row.put("CRIME_MATTER_TYPE", "01");
    row.put("TRAVEL_WAITING_COSTS", "0.00");
    row.put("WORK_CONCLUDED_DATE", formatDate(dates.workConcludedDate()));
    row.put("NO_OF_SUSPECTS", "1");
    row.put("NO_OF_POLICE_STATION", "1");
    row.put("POLICE_STATION", "NE001");
    row.put("DUTY_SOLICITOR", "N");
    row.put("YOUTH_COURT", "N");
    row.put("SCHEME_ID", "1001");
    row.put("DSCC_NUMBER", "200000000A");
    row.put("POSTAL_APPL_ACCP", "N");
    row.put("NATIONAL_REF_MECHANISM_ADVICE", "N");
    row.put("LEGACY_CASE", "N");
    row.put("LONDON_NONLONDON_RATE", "N");
    row.put("ADDITIONAL_TRAVEL_PAYMENT", "N");
    row.put("ELIGIBLE_CLIENT_INDICATOR", "Y");
    row.put("IRC_SURGERY", "N");
    row.put("SUBSTANTIVE_HEARING", "N");
    row.put("TOLERANCE_INDICATOR", "N");
    row.put("REP_ORDER_DATE", formatDate(dates.repOrderDate()));
    row.put("TRANSFER_DATE", formatDate(dates.workConcludedDate()));
    row.put("SURGERY_DATE", formatDate(dates.workConcludedDate()));
    row.put("CLIENT_LEGALLY_AIDED", "N");
    return row;
  }

  private static LinkedHashMap<String, String> buildMediationRow(
      GenerationContext context, OutcomeDates dates, int index) {
    LinkedHashMap<String, String> row = new LinkedHashMap<>();
    row.put("office", context.office());
    row.put("submissionPeriod", context.period());
    row.put("areaOfLaw", "Mediation");
    row.put("scheduleNum", context.scheduleNum());
    row.put("matterType", "MEDI:MDCS");
    row.put("FEE_CODE", "ASSA");
    row.put("CASE_START_DATE", formatDate(dates.caseStartDate()));
    row.put("CASE_ID", String.format(Locale.ROOT, "%03d", index));
    row.put("UFN", buildUfn(dates.caseStartDate(), index));
    row.put("CLIENT_FORENAME", "Client");
    row.put("CLIENT_SURNAME", "One" + index);
    row.put("CLIENT_DATE_OF_BIRTH", "01/01/1985");
    row.put("UCN", String.format(Locale.ROOT, "01011985/C/ONE%02d", index));
    row.put("GENDER", "F");
    row.put("ETHNICITY", "01");
    row.put("DISABILITY", "NCD");
    row.put("CLIENT_POST_CODE", "SE1 1AB");
    row.put("CLIENT_LEGALLY_AIDED", "Y");
    row.put("CLIENT2_FORENAME", "Client");
    row.put("CLIENT2_SURNAME", "Two" + index);
    row.put("CLIENT2_DATE_OF_BIRTH", "01/01/1986");
    row.put("CLIENT2_UCN", String.format(Locale.ROOT, "01011986/C/TWO%02d", index));
    row.put("CLIENT2_GENDER", "M");
    row.put("CLIENT2_ETHNICITY", "01");
    row.put("CLIENT2_DISABILITY", "NCD");
    row.put("CLIENT2_POST_CODE", "SE1 1AC");
    row.put("CLIENT2_LEGALLY_AIDED", "N");
    row.put("MED_CONCLUDED_DATE", formatDate(dates.medConcludedDate()));
    row.put("WORK_CONCLUDED_DATE", formatDate(dates.workConcludedDate()));
    row.put("NUMBER_OF_MEDIATION_SESSIONS", "2");
    row.put("MEDIATION_TIME", "120");
    row.put("CASE_REF_NUMBER", String.valueOf(1000 + index));
    row.put("OUTCOME_CODE", "B");
    row.put("DISBURSEMENTS_AMOUNT", "2.00");
    row.put("DISBURSEMENTS_VAT", "1.00");
    row.put("VAT_INDICATOR", "Y");
    row.put("UNIQUE_CASE_ID", row.get("UFN"));
    row.put("OUTREACH", "000");
    row.put("REFERRAL", "08");
    row.put("POSTAL_APPL_ACCP", "Y");
    row.put("CLIENT2_POSTAL_APPL_ACCP", "N");
    row.put("SCHEDULE_REF", context.scheduleNum());
    row.put("NATIONAL_REF_MECHANISM_ADVICE", "N");
    row.put("LEGACY_CASE", "N");
    row.put("LONDON_NONLONDON_RATE", "N");
    row.put("ADDITIONAL_TRAVEL_PAYMENT", "N");
    row.put("ELIGIBLE_CLIENT_INDICATOR", "Y");
    row.put("IRC_SURGERY", "N");
    row.put("SUBSTANTIVE_HEARING", "N");
    row.put("TOLERANCE_INDICATOR", "N");
    row.put("DUTY_SOLICITOR", "N");
    row.put("YOUTH_COURT", "N");
    return row;
  }

  private static Map<String, String> normalizeFeatureFields(Map<String, String> rawRow) {
    Map<String, String> row = new HashMap<>();
    if (rawRow == null) {
      return row;
    }

    for (Map.Entry<String, String> entry : rawRow.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (key == null || key.isBlank() || value == null || value.isBlank()) {
        continue;
      }

      String alias = key.trim().replaceAll("[ _-]", "").toUpperCase(Locale.ROOT);
      switch (alias) {
        case "OFFICE", "ACCOUNT" -> row.put("office", value);
        case "SUBMISSIONPERIOD" -> row.put("submissionPeriod", normalizeSubmissionPeriod(value));
        case "AREAOFLAW" -> row.put("areaOfLaw", normalizeAreaOfLaw(value));
        case "SCHEDULENUM" -> row.put("scheduleNum", value);
        case "MATTERTYPE" -> row.put("matterType", value);
        case "FEECODE" -> row.put("FEE_CODE", value);
        case "UFN", "UNIQUEFILENUMBER" -> row.put("UFN", value);
        case "UCN" -> row.put("UCN", value.toUpperCase(Locale.ROOT));
        case "PROFITCOST", "NETPROFITCOST", "NETPROFITCOSTS" -> row.put("PROFIT_COST", value);
        case "TRAVELCOST", "TRAVELCOSTS", "NETTRAVELCOST", "NETTRAVELCOSTS" -> row.put("TRAVEL_COSTS", value);
        case "TRAVELWAITINGCOSTS", "NETWAITINGCOST", "NETWAITINGCOSTS" -> row.put("TRAVEL_WAITING_COSTS", value);
        case "VATINDICATOR", "VATAPPLICABLE" -> row.put("VAT_INDICATOR", normalizeVat(value));
        case "NUMBEROFMEDIATIONSESSIONS", "SESSIONS" -> row.put("NUMBER_OF_MEDIATION_SESSIONS", value);
        case "DISBURSEMENTVATAMOUNT", "DISBURSEMENTSVAT", "DISBURSEMENTVAT" -> row.put("DISBURSEMENTS_VAT", value);
        case "NETDISBURSEMENTAMOUNT", "DISBURSEMENTAMOUNT", "DISBURSEMENTSAMOUNT" -> row.put("DISBURSEMENTS_AMOUNT", value);
        case "LONDONNONLONDONRATE" -> row.put("LONDON_NONLONDON_RATE", normalizeVat(value));
        case "REPRESENTATIONORDERDATE", "REPORDERDATE" ->
            row.put("REP_ORDER_DATE", normalizeDateValue(expandDatePlaceholder(value, rawRow)));
        case "CLIENTDATEOFBIRTH", "CLIENTDOB" ->
            row.put("CLIENT_DATE_OF_BIRTH", normalizeDateValue(value));
        case "CLIENT2DATEOFBIRTH" -> row.put("CLIENT2_DATE_OF_BIRTH", normalizeDateValue(value));
        case "CASECONCLUDEDDATE", "WORKCONCLUDEDDATE" ->
            row.put("WORK_CONCLUDED_DATE", normalizeDateValue(expandDatePlaceholder(value, rawRow)));
        case "MEDCONCLUDEDDATE" -> row.put("MED_CONCLUDED_DATE", normalizeDateValue(expandDatePlaceholder(value, rawRow)));
        case "IMMIGRATIONPRIORAUTHORITYNUMBER", "PRIORAUTHORITYREF" -> row.put("PRIOR_AUTHORITY_REF", value);
        case "JRFORMFILLING" -> row.put("JR_FORM_FILLING", value);
        case "DETENTIONTRAVELANDWAITINGCOSTS", "DETENTIONTRAVELWAITINGCOSTS" ->
            row.put("DETENTION_TRAVEL_WAITING_COSTS", value);
        case "BOLTONHOMEOFFICEINTERVIEW", "HOINTERVIEW" -> row.put("HO_INTERVIEW", value);
        case "BOLTONADJOURNEDHEARING", "ADJOURNEDHEARING" -> row.put("ADJOURNED_HEARING_FEE", value);
        case "BOLTONCMRHORAL", "CMRHORAL" -> row.put("CMRH_ORAL", value);
        case "BOLTONCMRHTELEPHONE", "CMRHTELEPHONE" -> row.put("CMRH_TELEPHONE", value);
        case "BOLTONSUBSTANTIVEHEARING", "SUBSTANTIVEHEARING" -> row.put("SUBSTANTIVE_HEARING", normalizeVat(value));
        case "STARTDATE", "CASESTARTDATE" ->
            row.put("CASE_START_DATE", normalizeDateValue(expandDatePlaceholder(value, rawRow)));
        case "TRANSFERDATE" -> row.put("TRANSFER_DATE", normalizeDateValue(expandDatePlaceholder(value, rawRow)));
        case "SURGERYDATE" -> row.put("SURGERY_DATE", normalizeDateValue(expandDatePlaceholder(value, rawRow)));
        case "OUTCOMECODE" -> row.put("OUTCOME_CODE", value);
        case "COUNSELCOST" -> row.put("COUNSEL_COST", value);
        case "NRMADVICE" -> row.put("NATIONAL_REF_MECHANISM_ADVICE", normalizeVat(value));
        case "LEGACYCASE" -> row.put("LEGACY_CASE", normalizeVat(value));
        case "ADDITIONALTRAVELPAYMENT" -> row.put("ADDITIONAL_TRAVEL_PAYMENT", normalizeVat(value));
        case "ELIGIBLECLIENTINDICATOR" -> row.put("ELIGIBLE_CLIENT_INDICATOR", normalizeVat(value));
        case "IRCSURGERY" -> row.put("IRC_SURGERY", normalizeVat(value));
        case "TOLERANCEINDICATOR" -> row.put("TOLERANCE_INDICATOR", normalizeVat(value));
        case "POSTALAPPLICATION" -> row.put("POSTAL_APPL_ACCP", normalizeVat(value));
        case "CLIENT2POSTALAPPLICATION" -> row.put("CLIENT2_POSTAL_APPL_ACCP", normalizeVat(value));
        case "CLIENTLEGALLYAIDED" -> row.put("CLIENT_LEGALLY_AIDED", normalizeVat(value));
        case "CLIENT2LEGALLYAIDED" -> row.put("CLIENT2_LEGALLY_AIDED", normalizeVat(value));
        case "POLICESTATION" -> row.put("POLICE_STATION", value);
        case "SCHEMEID" -> row.put("SCHEME_ID", value);
        case "DUTYSOLICITOR" -> row.put("DUTY_SOLICITOR", normalizeVat(value));
        case "YOUTHCOURT" -> row.put("YOUTH_COURT", normalizeVat(value));
         case "UNIQUECASEID" -> row.put("UNIQUE_CASE_ID", value);
         case "OUTREACH" -> row.put("OUTREACH", value);
         case "REFERRAL" -> row.put("REFERRAL", value);
         case "MEDIATIONTIME" -> row.put("MEDIATION_TIME", value);
         case "ESCAPECASE", "MESSAGES", "EXPECTEDTOTAL" -> {
           // Assertion-only fixture fields; do not write into upload payload.
         }
        default -> row.put(canonicalOutcomeKey(key), value);
      }
    }
    return row;
  }

  private static String expandDatePlaceholder(String value, Map<String, String> rawRow) {
    if (value == null || value.isBlank()) {
      return value;
    }
    if (!"later".equalsIgnoreCase(value.trim())) {
      return value;
    }

    String submissionPeriod = normalizeSubmissionPeriod(rawRow.getOrDefault("submissionPeriod", defaultSubmissionPeriod()));
    YearMonth period = parseSubmissionPeriod(submissionPeriod);
    // Use 21st of the month following the submission period to breach the "20th boundary" rule.
    // resolveContext() ensures the period is old enough that this date is already in the past.
    return period.plusMonths(1).atDay(21).format(SLASH_DATE);
  }

  private static String normalizeVat(String value) {
    if (value == null) {
      return null;
    }
    if ("Yes".equalsIgnoreCase(value) || "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
      return "Y";
    }
    if ("No".equalsIgnoreCase(value) || "N".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      return "N";
    }
    return value;
  }

  private static String normalizeDateValue(String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
      return value;
    }
    if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return formatDate(LocalDate.parse(value));
    }
    return value;
  }

  private static String defaultFeeCode(String areaOfLaw) {
    return switch (areaOfLaw.trim().toUpperCase(Locale.ROOT)) {
      case "CRIME LOWER", "CRIME" -> "APPA";
      case "MEDIATION" -> "ASSA";
      default -> "CAPA";
    };
  }

  private static String defaultMatterType(String areaOfLaw, String feeCode) {
    return switch (areaOfLaw.trim().toUpperCase(Locale.ROOT)) {
      case "CRIME LOWER", "CRIME" -> {
        String resolvedFeeCode = feeCode == null || feeCode.isBlank() ? defaultFeeCode(areaOfLaw) : feeCode;
        yield resolvedFeeCode.substring(0, Math.min(4, resolvedFeeCode.length()));
      }
      case "MEDIATION" -> "MEDI:MDCS";
      default -> "FAMX:FAPP";
    };
  }

  private static String normalizeAreaOfLaw(String areaOfLaw) {
    if (areaOfLaw == null) {
      return "Legal help";
    }
    return switch (areaOfLaw.trim().toUpperCase(Locale.ROOT)) {
      case "CRIME" -> "Crime lower";
      case "LEGAL HELP" -> "Legal help";
      case "MEDIATION" -> "Mediation";
      case "CRIME LOWER" -> "Crime lower";
      default -> areaOfLaw;
    };
  }

  private static String canonicalScheduleKey(String field) {
    if (field == null || field.isBlank()) {
      return field;
    }
    String upper = field.trim().replaceAll("[ _-]", "").toUpperCase(Locale.ROOT);
    return switch (upper) {
      case "SUBMISSIONPERIOD" -> "submissionPeriod";
      case "AREAOFLAW" -> "areaOfLaw";
      case "SCHEDULENUM" -> "scheduleNum";
      case "OFFICE", "ACCOUNT" -> "account";
      default -> field;
    };
  }

  private static String normalizeFieldAlias(String field) {
    if (field == null || field.isBlank()) {
      return "";
    }
    return field.trim().replaceAll("[ _-]", "").toUpperCase(Locale.ROOT);
  }

  private static String canonicalOutcomeKey(String field) {
    if (field == null || field.isBlank()) {
      return field;
    }
    String upper = field.trim().replaceAll("[ _-]", "").toUpperCase(Locale.ROOT);
    return switch (upper) {
      case "FEECODE" -> "FEE_CODE";
      case "PROFITCOST", "NETPROFITCOST", "NETPROFITCOSTS" -> "PROFIT_COST";
      case "DISBURSEMENTSAMOUNT", "NETDISBURSEMENTAMOUNT", "DISBURSEMENTAMOUNT" -> "DISBURSEMENTS_AMOUNT";
      case "DISBURSEMENTSVAT", "DISBURSEMENTVATAMOUNT", "DISBURSEMENTVAT" -> "DISBURSEMENTS_VAT";
      case "UFN", "UNIQUEFILENUMBER" -> "UFN";
      case "UCN" -> "UCN";
      case "CASESTARTDATE", "STARTDATE" -> "CASE_START_DATE";
      case "WORKCONCLUDEDDATE", "CASECONCLUDEDDATE" -> "WORK_CONCLUDED_DATE";
      case "MEDCONCLUDEDDATE" -> "MED_CONCLUDED_DATE";
      case "CLIENTDATEOFBIRTH", "CLIENTDOB" -> "CLIENT_DATE_OF_BIRTH";
      case "CLIENT2DATEOFBIRTH" -> "CLIENT2_DATE_OF_BIRTH";
      case "TRANSFERDATE" -> "TRANSFER_DATE";
      case "SURGERYDATE" -> "SURGERY_DATE";
      case "REPRESENTATIONORDERDATE", "REPORDERDATE" -> "REP_ORDER_DATE";
      case "TRAVELTIME" -> "TRAVEL_TIME";
      case "WAITINGTIME" -> "WAITING_TIME";
      case "TRAVELCOST", "TRAVELCOSTS", "NETTRAVELCOST", "NETTRAVELCOSTS" -> "TRAVEL_COSTS";
      case "TRAVELWAITINGCOSTS", "NETWAITINGCOST", "NETWAITINGCOSTS" -> "TRAVEL_WAITING_COSTS";
      case "VATINDICATOR", "VATAPPLICABLE" -> "VAT_INDICATOR";
      case "LONDONNONLONDONRATE" -> "LONDON_NONLONDON_RATE";
       case "NUMBEROFMEDIATIONSESSIONS", "SESSIONS" -> "NUMBER_OF_MEDIATION_SESSIONS";
       case "MEDIATIONTIME" -> "MEDIATION_TIME";
       case "UNIQUECASEID" -> "UNIQUE_CASE_ID";
       case "OUTREACH" -> "OUTREACH";
       case "REFERRAL" -> "REFERRAL";
      case "POSTALAPPLICATION" -> "POSTAL_APPL_ACCP";
      case "CLIENT2POSTALAPPLICATION" -> "CLIENT2_POSTAL_APPL_ACCP";
      case "NRMADVICE" -> "NATIONAL_REF_MECHANISM_ADVICE";
      case "LEGACYCASE" -> "LEGACY_CASE";
      case "ADDITIONALTRAVELPAYMENT" -> "ADDITIONAL_TRAVEL_PAYMENT";
      case "ELIGIBLECLIENTINDICATOR" -> "ELIGIBLE_CLIENT_INDICATOR";
      case "IRCSURGERY" -> "IRC_SURGERY";
      case "SUBSTANTIVEHEARING" -> "SUBSTANTIVE_HEARING";
      case "TOLERANCEINDICATOR" -> "TOLERANCE_INDICATOR";
      case "HOINTERVIEW", "BOLTONHOMEOFFICEINTERVIEW" -> "HO_INTERVIEW";
      case "ADJOURNEDHEARING", "BOLTONADJOURNEDHEARING" -> "ADJOURNED_HEARING_FEE";
      case "CMRHORAL", "BOLTONCMRHORAL" -> "CMRH_ORAL";
      case "CMRHTELEPHONE", "BOLTONCMRHTELEPHONE" -> "CMRH_TELEPHONE";
      case "IMMIGRATIONPRIORAUTHORITYNUMBER", "PRIORAUTHORITYREF" -> "PRIOR_AUTHORITY_REF";
      case "JRFORMFILLING" -> "JR_FORM_FILLING";
      case "DETENTIONTRAVELWAITINGCOSTS", "DETENTIONTRAVELANDWAITINGCOSTS" -> "DETENTION_TRAVEL_WAITING_COSTS";
      case "POLICESTATION" -> "POLICE_STATION";
      case "SCHEMEID" -> "SCHEME_ID";
      case "DUTYSOLICITOR" -> "DUTY_SOLICITOR";
      case "YOUTHCOURT" -> "YOUTH_COURT";
      case "CLIENTLEGALLYAIDED" -> "CLIENT_LEGALLY_AIDED";
      case "CLIENT2LEGALLYAIDED" -> "CLIENT2_LEGALLY_AIDED";
      default -> field;
    };
  }

  private static String replaceOrAppendToken(String line, String key, String value) {
    String regex = "(^|,)" + java.util.regex.Pattern.quote(key) + "=[^,]*";
    if (line.matches(".*" + regex + ".*")) {
      return line.replaceFirst(regex, "$1" + key + "=" + value);
    }
    return line + "," + key + "=" + value;
  }

  private static String scheduleNumForArea(String areaOfLaw, String office, String period) {
    String normalized = normalizeAreaOfLaw(areaOfLaw);
    if ("Crime lower".equalsIgnoreCase(normalized)) {
      return office + "/CRM";
    }
    if ("Mediation".equalsIgnoreCase(normalized)) {
      String mon = period.substring(0, 3).toUpperCase(Locale.ROOT);
      String yr = period.substring(period.length() - 2);
      return office + "/MEDI" + mon + yr + "/01";
    }
    return office + "/CIVIL";
  }

  private static String buildRecordDelimited(String areaOfLaw, GenerationContext context, List<Map<String, String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("OFFICE,account=").append(context.office()).append('\n');
    sb.append("SCHEDULE,submissionPeriod=")
        .append(context.period())
        .append(",areaOfLaw=")
        .append(normalizeAreaOfLaw(areaOfLaw).toUpperCase(Locale.ROOT))
        .append(",scheduleNum=")
        .append(context.scheduleNum())
        .append('\n');

    for (Map<String, String> row : rows) {
      String matterType = row.getOrDefault("matterType", defaultMatterType(areaOfLaw, row.get("FEE_CODE")));
      sb.append("OUTCOME,matterType=").append(matterType);
      for (String key : orderedOutcomeKeys(areaOfLaw, row)) {
        if (isReservedOutcomeKey(key)) {
          continue;
        }
        String value = row.get(key);
        if (value == null || value.isBlank()) {
          continue;
        }
        sb.append(',').append(canonicalOutcomeKey(key)).append('=').append(value);
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  private static String buildXmlSubmission(String areaOfLaw, GenerationContext context, List<Map<String, String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<submission xmlns=\"http://www.legalservices.gov.uk/sms/ActivityManagement/XMLSchema/\" ")
        .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
        .append("schemaLocation=\"")
        .append(XML_SCHEMA_LOCATION)
        .append("\">\n");
    sb.append("  <office account=\"").append(escapeXml(context.office())).append("\">\n");
    sb.append("    <schedule submissionPeriod=\"")
        .append(escapeXml(context.period()))
        .append("\" areaOfLaw=\"")
        .append(escapeXml(normalizeAreaOfLaw(areaOfLaw).toUpperCase(Locale.ROOT)))
        .append("\" scheduleNum=\"")
        .append(escapeXml(context.scheduleNum()))
        .append("\">\n");
    for (Map<String, String> row : rows) {
      String matterType = row.getOrDefault("matterType", defaultMatterType(areaOfLaw, row.get("FEE_CODE")));
      sb.append("      <outcome matterType=\"").append(escapeXml(matterType)).append("\">\n");
      for (String key : orderedOutcomeKeys(areaOfLaw, row)) {
        if (isReservedOutcomeKey(key)) {
          continue;
        }
        String value = row.get(key);
        if (value == null || value.isBlank()) {
          continue;
        }
        sb.append("        <outcomeItem name=\"")
            .append(escapeXml(canonicalOutcomeKey(key)))
            .append("\">")
            .append(escapeXml(value))
            .append("</outcomeItem>\n");
      }
      sb.append("      </outcome>\n");
    }
    sb.append("    </schedule>\n");
    sb.append("  </office>\n");
    sb.append("</submission>\n");
    return sb.toString();
  }

  private static List<String> orderedOutcomeKeys(String areaOfLaw, Map<String, String> row) {
    LinkedHashSet<String> ordered = new LinkedHashSet<>();
    ordered.addAll(
        switch (normalizeAreaOfLaw(areaOfLaw).toUpperCase(Locale.ROOT)) {
          case "CRIME LOWER", "CRIME" -> CRIME_ORDER;
          case "MEDIATION" -> MEDIATION_ORDER;
          default -> LEGAL_HELP_ORDER;
        });
    ordered.addAll(row.keySet());
    return new ArrayList<>(ordered);
  }

  private static boolean isReservedOutcomeKey(String key) {
    return "matterType".equals(key)
        || "office".equals(key)
        || "submissionPeriod".equals(key)
        || "areaOfLaw".equals(key)
        || "scheduleNum".equals(key);
  }

  private static String escapeXml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

   private static OutcomeDates deriveOutcomeDates(GenerationContext context) {
     YearMonth submissionPeriod = parseSubmissionPeriod(context.period());
     LocalDate latestAllowed = submissionPeriod.plusMonths(1).atDay(20);
       LocalDate today = LocalDate.now();
       if (latestAllowed.isAfter(today)) {
         latestAllowed = today;
       }

    LocalDate scheduleStart = parseFlexibleDate(context.scheduleStart());
    LocalDate scheduleEnd = parseFlexibleDate(context.scheduleEnd());
      if (scheduleStart != null && scheduleStart.isAfter(today)) {
        scheduleStart = today;
      }
      if (scheduleEnd != null && scheduleEnd.isAfter(today)) {
        scheduleEnd = today;
      }
    if (scheduleEnd != null && scheduleEnd.isBefore(latestAllowed)) {
      latestAllowed = scheduleEnd;
    }

    LocalDate caseStart = submissionPeriod.atDay(1).minusMonths(3);
    if (scheduleStart != null && caseStart.isBefore(scheduleStart)) {
      caseStart = scheduleStart;
    }
    if (caseStart.isAfter(latestAllowed)) {
      caseStart = latestAllowed;
    }

    LocalDate workConcluded = caseStart.plusDays(1);
    if (workConcluded.isAfter(latestAllowed)) {
      workConcluded = latestAllowed;
    }
    if (workConcluded.isBefore(caseStart)) {
      workConcluded = caseStart;
    }

    LocalDate repOrder = caseStart;
    LocalDate medConcluded = workConcluded.plusDays(1);
    if (medConcluded.isAfter(latestAllowed)) {
      medConcluded = latestAllowed;
    }
    if (medConcluded.isBefore(workConcluded)) {
      medConcluded = workConcluded;
    }

    return new OutcomeDates(caseStart, workConcluded, repOrder, medConcluded);
  }

  private static LocalDate parseFlexibleDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException ignored) {
      try {
        return LocalDate.parse(value, SLASH_DATE);
      } catch (DateTimeParseException ignoredAgain) {
        return null;
      }
    }
  }

  private static String formatDate(LocalDate date) {
    return date.format(SLASH_DATE);
  }

  private static String buildUfn(LocalDate caseStartDate, int index) {
    return caseStartDate.format(DateTimeFormatter.ofPattern("ddMMyy", Locale.UK))
        + "/"
        + String.format(Locale.ROOT, "%03d", index);
  }

  private static String normalizeSubmissionPeriod(String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    try {
      return parseSubmissionPeriod(value).format(SHORT_PERIOD).toUpperCase(Locale.ROOT);
    } catch (DateTimeParseException ignored) {
      return value.trim().toUpperCase(Locale.ROOT);
    }
  }

   private static YearMonth parseSubmissionPeriod(String value) {
     String sanitized = value == null ? "" : value.replaceAll("[^A-Za-z0-9-]", "").trim();
     // Ensure proper format: extract month and year, then format as "MMM-yyyy"
     String[] parts = sanitized.split("-");
     if (parts.length >= 2) {
       String month = parts[0].toUpperCase(Locale.ROOT);
       String year = parts[1];
       // Ensure month is properly formatted (3 uppercase letters)
       if (month.length() == 3) {
         // Convert to title case (e.g., "SEP" -> "Sep")
         String monthTitleCase = month.substring(0, 1).toUpperCase(Locale.ROOT) + month.substring(1).toLowerCase(Locale.ROOT);
         sanitized = monthTitleCase + "-" + year;
       }
     }
     try {
       return YearMonth.parse(sanitized, SHORT_PERIOD_PARSER);
     } catch (Exception e) {
       throw new DateTimeParseException("Unable to parse submission period: " + value + " (sanitized: " + sanitized + ")", sanitized, 0, e);
     }
   }

  private static String defaultSubmissionPeriod() {
    return YearMonth.now().minusMonths(1).format(SHORT_PERIOD).toUpperCase(Locale.ROOT);
  }

  private static String defaultOffice() {
    return "0P322F";
  }

  private static String firstNonBlank(List<Map<String, String>> rows, List<String> candidateKeys, String defaultValue) {
    for (Map<String, String> row : rows) {
      if (row == null) {
        continue;
      }
      for (String key : candidateKeys) {
        String value = row.get(key);
        if (value != null && !value.isBlank()) {
          return value;
        }
      }
    }
    return defaultValue;
  }

  private static void ensureDirExists(Path dir) throws IOException {
    if (dir != null && !Files.exists(dir)) {
      Files.createDirectories(dir);
    }
  }

  private static void waitForFile(Path filePath, long timeoutMs) throws IOException {
    long start = System.currentTimeMillis();
    while (!Files.exists(filePath)) {
      if (System.currentTimeMillis() - start > timeoutMs) {
        throw new IOException("File " + filePath + " not found after " + timeoutMs + "ms");
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while waiting for file", e);
      }
    }
  }

  private record GenerationContext(
      String office, String period, String scheduleStart, String scheduleEnd, String scheduleNum) {}

  private record OutcomeDates(
      LocalDate caseStartDate,
      LocalDate workConcludedDate,
      LocalDate repOrderDate,
      LocalDate medConcludedDate) {}
}
