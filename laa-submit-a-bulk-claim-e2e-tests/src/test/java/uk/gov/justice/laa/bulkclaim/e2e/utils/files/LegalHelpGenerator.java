package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;

/**
 * Direct Java equivalent to generateCivilFiles.ts
 * Generates Legal Help (Civil) submission files with randomized, realistic data.
 */
public final class LegalHelpGenerator {

  private static final DateTimeFormatter SLASH_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK);
  private static final DateTimeFormatter SHORT_PERIOD =
      DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter SHORT_PERIOD_PARSER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("MMM-yyyy")
          .toFormatter(Locale.ENGLISH);

  private static final List<String> OFFICES = List.of("0P322F", "2L847Q", "2N199K", "2P746R", "1T102C");
  private static final List<String> FEE_CODES = List.of("CAPA", "COM");
  private static final LocalDate MIN_CASE_DATE = LocalDate.of(1995, 1, 1);

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  private LegalHelpGenerator() {}

  /**
   * TS-like entrypoint parity:
   * GenerateCivilFile(files, outcomes, format, options) => { filePaths, office }
   */
  public static GeneratorResult GenerateCivilFile(
      int files,
      int outcomes,
      String format,
      GenerateFileOptions options)
      throws IOException {
    List<String> paths = new ArrayList<>();
    String office =
        options != null && options.getOffice() != null && !options.getOffice().isBlank()
            ? options.getOffice()
            : randomFrom(OFFICES);

    for (int i = 1; i <= Math.max(1, files); i++) {
      String suffix =
          options != null && options.getSuffix() != null && !options.getSuffix().isBlank()
              ? options.getSuffix()
              : System.currentTimeMillis() + "_" + RANDOM.nextInt(10_000);
      String baseName = "legal_" + suffix + "_" + i;
      String normalized = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
      String intermediate = "xml".equals(normalized) ? "csv" : normalized;

      Path out = Paths.get("build", "tmp", "e2e", "generated", baseName + "." + intermediate);
      List<Map<String, String>> claims =
          options != null && options.getClaims() != null ? options.getClaims() : List.of();

      Path generated = generateFromClaimsTable(intermediate, claims.isEmpty() ? defaultClaimRows(outcomes) : claims, out, office);
      paths.add(generated.toString());
    }

    return new GeneratorResult(paths, office);
  }

  /**
   * Generate a Legal Help submission file with the specified number of outcomes.
   */
  public static Path generateMinimalSubmissionFile(String format, int outcomes, Path filePath)
      throws IOException {
    String office = randomFrom(OFFICES);
    List<Map<String, String>> rows = new ArrayList<>();
    for (int i = 0; i < outcomes; i++) {
      rows.add(new HashMap<>()); // Empty overrides
    }
    return generateFromClaimsTable(format, rows, filePath, office);
  }

  private static List<Map<String, String>> defaultClaimRows(int outcomes) {
    List<Map<String, String>> rows = new ArrayList<>();
    for (int i = 0; i < Math.max(1, outcomes); i++) {
      rows.add(new HashMap<>());
    }
    return rows;
  }

  /**
   * Generate a Legal Help submission file from a claims table (with optional overrides).
   */
  public static Path generateFromClaimsTable(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    String office =
        claimRows.isEmpty() ? randomFrom(OFFICES) : extractOfficeOrDefault(claimRows.get(0));
    return generateFromClaimsTable(format, claimRows, filePath, office);
  }

   private static Path generateFromClaimsTable(
       String format,
       List<Map<String, String>> claimRows,
       Path filePath,
       String office)
       throws IOException {
     try {
       ensureDirExists(filePath.getParent());

        List<Map<String, String>> normalizedClaimRows = normalizeClaimOverrides(claimRows);

       String feeCode =
            normalizedClaimRows.isEmpty()
               ? randomFrom(FEE_CODES)
                : normalizedClaimRows.get(0).getOrDefault("feeCode", randomFrom(FEE_CODES));

        SubmissionPeriodHelper.SubmissionPeriodResult period;
        try {
          period = SubmissionPeriodHelper.getUniqueSubmissionPeriod(office, "LEGAL HELP", feeCode);
        } catch (Exception e) {
          // Fallback to default period if no valid submission period available
          String fallbackPeriod = getDefaultSubmissionPeriod();
          period = new SubmissionPeriodHelper.SubmissionPeriodResult(fallbackPeriod, null, null);
        }

       String content = "xml".equalsIgnoreCase(format)
            ? buildXmlFileContent(office, period, normalizedClaimRows)
            : buildFileContent(office, period, normalizedClaimRows);

       Files.writeString(filePath, content, StandardCharsets.UTF_8);
       waitForFile(filePath, 5000);
       return filePath;
     } catch (Exception e) {
       throw new IOException("Failed to generate Legal Help file: " + e.getMessage(), e);
     }
   }

  private static List<Map<String, String>> normalizeClaimOverrides(List<Map<String, String>> claimRows) {
    if (claimRows == null || claimRows.isEmpty()) {
      return List.of();
    }

    List<Map<String, String>> normalized = new ArrayList<>();
    for (Map<String, String> row : claimRows) {
      Map<String, String> mapped = new HashMap<>();
      if (row == null || row.isEmpty()) {
        normalized.add(mapped);
        continue;
      }

      // Keep original keys and add TS-compatible aliases used by Java generator.
      mapped.putAll(row);

      if (row.containsKey("netDisbursementAmount")) {
        mapped.put("disbursementsAmount", row.get("netDisbursementAmount"));
      }
      if (row.containsKey("disbursementVatAmount")) {
        mapped.put("disbursementsVat", row.get("disbursementVatAmount"));
      }
      if (row.containsKey("netProfitCosts")) {
        mapped.put("profitCost", row.get("netProfitCosts"));
      }
      if (row.containsKey("netCostOfCounsel")) {
        mapped.put("counselCost", row.get("netCostOfCounsel"));
      }
      if (row.containsKey("vatApplicable") && !row.containsKey("vatIndicator")) {
        mapped.put("vatIndicator", row.get("vatApplicable"));
      }
      if (row.containsKey("FEE_CODE") && !row.containsKey("feeCode")) {
        mapped.put("feeCode", row.get("FEE_CODE"));
      }

      normalized.add(mapped);
    }
    return normalized;
  }

  private static String buildFileContent(
      String office,
      SubmissionPeriodHelper.SubmissionPeriodResult period,
      List<Map<String, String>> claimRows)
      throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("OFFICE,account=").append(office).append("\n");
    sb.append("SCHEDULE,submissionPeriod=")
        .append(period.period())
        .append(",areaOfLaw=LEGAL HELP,scheduleNum=")
        .append(office)
        .append("/CIVIL\n");

    LocalDate scheduleStart =
        period.scheduleStart() != null ? LocalDate.parse(period.scheduleStart()) : MIN_CASE_DATE;
    LocalDate scheduleEnd =
        period.scheduleEnd() != null ? LocalDate.parse(period.scheduleEnd()) : LocalDate.now();

    for (int i = 0; i < claimRows.size(); i++) {
      Map<String, String> override = claimRows.get(i);
      OutcomeData outcome =
          generateOutcome(office, i, scheduleStart, scheduleEnd, period.period(), override);
      sb.append(formatOutcome(outcome, override)).append("\n");
    }

     return sb.toString();
   }

   private static String buildXmlFileContent(
       String office,
       SubmissionPeriodHelper.SubmissionPeriodResult period,
       List<Map<String, String>> claimRows)
       throws Exception {
     StringBuilder sb = new StringBuilder();
     sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
     sb.append("<submission xmlns=\"http://www.legalservices.gov.uk/sms/ActivityManagement/XMLSchema/\" ")
         .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
         .append("xsi:schemaLocation=\"http://www.legalservices.gov.uk/sms/ActivityManagement/XMLSchema/LSCSMSBulkLoadSchemaV3.xsd\">\n");
     sb.append("  <office account=\"").append(escapeXml(office)).append("\">\n");
     sb.append("    <schedule submissionPeriod=\"")
         .append(escapeXml(period.period()))
         .append("\" areaOfLaw=\"LEGAL HELP\" scheduleNum=\"")
         .append(escapeXml(office))
         .append("/CIVIL\">\n");

     LocalDate scheduleStart =
         period.scheduleStart() != null ? LocalDate.parse(period.scheduleStart()) : MIN_CASE_DATE;
     LocalDate scheduleEnd =
         period.scheduleEnd() != null ? LocalDate.parse(period.scheduleEnd()) : LocalDate.now();

     for (int i = 0; i < claimRows.size(); i++) {
       Map<String, String> override = claimRows.get(i);
       OutcomeData outcome =
           generateOutcome(office, i, scheduleStart, scheduleEnd, period.period(), override);
       String matterType = override.getOrDefault("matterType", "FAMX:FAPP");
       sb.append("      <outcome matterType=\"").append(escapeXml(matterType)).append("\">\n");

       // Build XML items from outcome
       Map<String, String> outcomeFields = buildOutcomeXmlFields(outcome, override);
       for (Map.Entry<String, String> entry : outcomeFields.entrySet()) {
         sb.append("        <outcomeItem name=\"")
             .append(escapeXml(entry.getKey()))
             .append("\">")
             .append(escapeXml(entry.getValue()))
             .append("</outcomeItem>\n");
       }
       sb.append("      </outcome>\n");
     }

     sb.append("    </schedule>\n");
     sb.append("  </office>\n");
     sb.append("</submission>\n");
     return sb.toString();
   }

   private static Map<String, String> buildOutcomeXmlFields(OutcomeData data, Map<String, String> override) {
     Map<String, String> fields = new LinkedHashMap<>();
     fields.put("FEE_CODE", override.getOrDefault("feeCode", randomFrom(FEE_CODES)));
     fields.put("CASE_REF_NUMBER", data.caseRefNumber);
     fields.put("CASE_START_DATE", data.caseStartDate);
     fields.put("CASE_ID", data.caseId);
     fields.put("UFN", data.ufn);
     fields.put("PROCUREMENT_AREA", data.procurementArea);
     fields.put("ACCESS_POINT", data.accessPoint);
     fields.put("CLIENT_FORENAME", data.clientForename);
     fields.put("CLIENT_SURNAME", data.clientSurname);
     fields.put("CLIENT_DATE_OF_BIRTH", data.clientDateOfBirth);
     fields.put("UCN", data.ucn);
     fields.put("GENDER", data.gender);
     fields.put("ETHNICITY", data.ethnicity);
     fields.put("DISABILITY", data.disability);
     fields.put("CLIENT_POST_CODE", data.clientPostCode);
     fields.put("WORK_CONCLUDED_DATE", data.workConcludedDate);
     fields.put("CASE_STAGE_LEVEL", data.caseStageLevel);
     fields.put("ADVICE_TIME", data.adviceTime);
     fields.put("TRAVEL_TIME", data.travelTime);
     fields.put("WAITING_TIME", data.waitingTime);
     fields.put("PROFIT_COST", data.profitCost);
     fields.put("DISBURSEMENTS_AMOUNT", data.disbursementsAmount);
     fields.put("COUNSEL_COST", data.counselCost);
     fields.put("DISBURSEMENTS_VAT", data.disbursementsVat);
     fields.put("TRAVEL_WAITING_COSTS", data.travelWaitingCosts);
     fields.put("VAT_INDICATOR", data.vatIndicator);
     fields.put("LONDON_NONLONDON_RATE", data.londonNonLondonRate);
     fields.put("TRAVEL_COSTS", data.travelCosts);
     fields.put("OUTCOME_CODE", data.outcomeCode);
     fields.put("POSTAL_APPL_ACCP", data.postalApplAccp);
     fields.put("NATIONAL_REF_MECHANISM_ADVICE", data.nationalRefMechanismAdvice);
     fields.put("LEGACY_CASE", data.legacyCase);
     fields.put("ADDITIONAL_TRAVEL_PAYMENT", data.additionalTravelPayment);
     fields.put("ELIGIBLE_CLIENT_INDICATOR", data.eligibleClientIndicator);
     fields.put("IRC_SURGERY", data.ircSurgery);
     fields.put("SUBSTANTIVE_HEARING", data.substantiveHearing);
     fields.put("TOLERANCE_INDICATOR", data.toleranceIndicator);
     fields.put("SURGERY_DATE", data.surgeryDate);
     fields.put("REP_ORDER_DATE", data.repOrderDate);
     fields.put("TRANSFER_DATE", data.transferDate);
     fields.put("SCHEDULE_REF", data.scheduleRef);
     return fields;
    }

   private static OutcomeData generateOutcome(
       String office,
       int caseNum,
       LocalDate scheduleStart,
       LocalDate scheduleEnd,
       String submissionPeriod,
       Map<String, String> override)
       throws Exception {
     String firstName = override.getOrDefault("firstName", "Test");
     String lastName = override.getOrDefault("lastName", "User" + (caseNum + 1));

     LocalDate start = scheduleStart != null ? scheduleStart : MIN_CASE_DATE;
     LocalDate end = scheduleEnd != null ? scheduleEnd : LocalDate.now();

     // Adjust end to last day of month before submission period
     YearMonth period = YearMonth.parse(submissionPeriod, SHORT_PERIOD_PARSER);
     end = period.atDay(1).minusDays(1);

     // Ensure valid range
     if (end.isBefore(start)) {
       end = start.plusMonths(1);
       if (end.isAfter(LocalDate.now())) {
         end = LocalDate.now();
       }
     }

     LocalDate caseStart = randomDateBetween(start, end);
     LocalDate concluded = randomDateBetween(caseStart, end);
     
     // Handle DOB override - if invalid date provided, still generate valid dob for internal logic
     // Invalid dates will be used in the CSV and caught by backend validation
     LocalDate dob = parseDateOrUseRandom(override.getOrDefault("clientDateOfBirth", 
         override.getOrDefault("clientDob", null)), LocalDate.of(1960, 1, 1), LocalDate.of(2005, 12, 31));
     
     LocalDate repOrder = randomDateBetween(LocalDate.of(2016, 4, 1), end);

     String ufn = override.getOrDefault("ufn", buildUfn(caseStart, caseNum));
     String ucn =
         override.getOrDefault(
             "ucn",
             buildUcn(dob, lastName, firstName));

     return new OutcomeData()
         .caseRefNumber(
             override.getOrDefault(
                 "caseRefNumber",
                 clean(firstName.substring(0, Math.min(3, firstName.length())))
                     + "/"
                     + clean(lastName)))
         .caseStartDate(
             handleDateOverride(override.get("caseStartDate"), formatDate(caseStart)))
         .caseId(padNum(caseNum + 1, 3))
         .ufn(ufn)
         .procurementArea("PA00120")
         .accessPoint("AP00000")
         .clientForename(firstName)
         .clientSurname(lastName)
         .clientDateOfBirth(
             handleDateOverride(override.get("clientDateOfBirth") != null ? override.get("clientDateOfBirth") : override.get("clientDob"), formatDate(dob)))
         .ucn(ucn)
         .gender(randomFrom(List.of("M", "F")))
         .ethnicity("12")
         .disability("NCD")
         .clientPostCode(
             override.getOrDefault("clientPostCode", randomPostCode()))
         .workConcludedDate(
             handleWorkConcludedDateOverride(override, concluded, submissionPeriod))
         .caseStageLevel("FPC01")
         .adviceTime("120")
         .travelTime("0")
         .waitingTime("0")
         .profitCost(
             override.getOrDefault("profitCost", String.format("%.2f", randomAmount(50, 200))))
         .disbursementsAmount(
             override.getOrDefault(
                 "disbursementsAmount", String.format("%.2f", randomAmount(0, 20))))
         .counselCost("19.33")
         .disbursementsVat(
             override.getOrDefault(
                 "disbursementsVat", String.format("%.2f", randomAmount(0, 1.98))))
         .travelWaitingCosts("0.00")
         .vatIndicator(override.getOrDefault("vatIndicator", "Y"))
         .londonNonLondonRate(override.getOrDefault("londonNonLondonRate", "N"))
         .travelCosts("5.86")
         .outcomeCode(override.getOrDefault("outcomeCode", "FX"))
         .postalApplAccp(override.getOrDefault("postalApplAccp", "Y"))
         .nationalRefMechanismAdvice(override.getOrDefault("nrmAdvice", "Y"))
         .legacyCase(override.getOrDefault("legacyCase", "N"))
         .additionalTravelPayment(override.getOrDefault("additionalTravelPayment", "N"))
         .eligibleClientIndicator(override.getOrDefault("eligibleClientIndicator", "Y"))
         .ircSurgery(override.getOrDefault("ircSurgery", "N"))
         .substantiveHearing(override.getOrDefault("substantiveHearing", "N"))
         .toleranceIndicator(override.getOrDefault("toleranceIndicator", "N"))
         .surgeryDate(
             handleDateOverride(override.get("surgeryDate"), formatDate(concluded)))
         .repOrderDate(override.getOrDefault("repOrderDate", formatDate(repOrder)))
         .transferDate(
             handleDateOverride(override.get("transferDate"), formatDate(concluded)))
         .scheduleRef(
             override.getOrDefault(
                 "scheduleRef", office + "/" + LocalDate.now().getYear() + "/" + (caseNum + 1)));
   }

  private static String formatOutcome(OutcomeData data, Map<String, String> override) {
    String feeCode = override.getOrDefault("feeCode", randomFrom(FEE_CODES));
    
    // For date fields, resolve "later" keyword first, then use overrides as-is if provided (allows invalid dates for validation testing)
    String caseStartDate = override.getOrDefault("caseStartDate", data.caseStartDate);
    String workConcludedDateOverride = override.get("workConcludedDate");
    String workConcludedDate;
    if (workConcludedDateOverride != null && workConcludedDateOverride.toLowerCase(Locale.ROOT).contains("later")) {
      // "later" means a date after the 20th of the month following submission period
      // This will trigger: "Case Concluded Date cannot be later than the 20th of the month following the submission period"
      workConcludedDate = data.workConcludedDate; // workConcludedDate is already handled by generateOutcome()
    } else {
      workConcludedDate = workConcludedDateOverride != null ? workConcludedDateOverride : data.workConcludedDate;
    }
    String clientDateOfBirth = override.getOrDefault("clientDateOfBirth", 
        override.getOrDefault("clientDob", data.clientDateOfBirth));
    String surgeryDate = override.getOrDefault("surgeryDate", data.surgeryDate);
    String transferDate = override.getOrDefault("transferDate", data.transferDate);
    String repOrderDate = override.getOrDefault("repOrderDate", data.repOrderDate);
    
    // For Y/N fields, use overrides as-is if provided (allows invalid values like "A" for backend validation testing)
    String vatIndicator = override.getOrDefault("vatApplicable", 
        override.getOrDefault("vatIndicator", data.vatIndicator));
    String postalApplAccp = override.getOrDefault("postalApplication", 
        override.getOrDefault("postalApplAccp", data.postalApplAccp));
    String nrmAdvice = override.getOrDefault("nrmAdvice", data.nationalRefMechanismAdvice);
    String londonNonLondonRate = override.getOrDefault("londonNonLondonRate", data.londonNonLondonRate);
    String legacyCase = override.getOrDefault("legacyCase", data.legacyCase);
    String additionalTravelPayment = override.getOrDefault("additionalTravelPayment", data.additionalTravelPayment);
    String eligibleClientIndicator = override.getOrDefault("eligibleClientIndicator", data.eligibleClientIndicator);
    String ircSurgery = override.getOrDefault("ircSurgery", data.ircSurgery);
    String substantiveHearing = override.getOrDefault("substantiveHearing", data.substantiveHearing);
    String toleranceIndicator = override.getOrDefault("toleranceIndicator", data.toleranceIndicator);
    
    return "OUTCOME,"
        + "FEE_CODE=" + feeCode + ","
        + "matterType=FAMX:FAPP,"
        + "CASE_REF_NUMBER=" + data.caseRefNumber + ","
        + "CASE_START_DATE=" + caseStartDate + ","
        + "CASE_ID=" + data.caseId + ","
        + "UFN=" + data.ufn + ","
        + "PROCUREMENT_AREA=" + data.procurementArea + ","
        + "ACCESS_POINT=" + data.accessPoint + ","
        + "CLIENT_FORENAME=" + data.clientForename + ","
        + "CLIENT_SURNAME=" + data.clientSurname + ","
        + "CLIENT_DATE_OF_BIRTH=" + clientDateOfBirth + ","
        + "UCN=" + data.ucn + ","
        + "GENDER=" + data.gender + ","
        + "ETHNICITY=" + data.ethnicity + ","
        + "DISABILITY=" + data.disability + ","
        + "CLIENT_POST_CODE=" + data.clientPostCode + ","
        + "WORK_CONCLUDED_DATE=" + workConcludedDate + ","
        + "CASE_STAGE_LEVEL=" + data.caseStageLevel + ","
        + "ADVICE_TIME=" + data.adviceTime + ","
        + "TRAVEL_TIME=" + data.travelTime + ","
        + "WAITING_TIME=" + data.waitingTime + ","
        + "PROFIT_COST=" + data.profitCost + ","
        + "DISBURSEMENTS_AMOUNT=" + data.disbursementsAmount + ","
        + "COUNSEL_COST=" + data.counselCost + ","
        + "DISBURSEMENTS_VAT=" + data.disbursementsVat + ","
        + "TRAVEL_WAITING_COSTS=" + data.travelWaitingCosts + ","
        + "VAT_INDICATOR=" + vatIndicator + ","
        + "LONDON_NONLONDON_RATE=" + londonNonLondonRate + ","
        + "TRAVEL_COSTS=" + data.travelCosts + ","
        + "OUTCOME_CODE=" + data.outcomeCode + ","
        + "POSTAL_APPL_ACCP=" + postalApplAccp + ","
        + "NATIONAL_REF_MECHANISM_ADVICE=" + nrmAdvice + ","
        + "LEGACY_CASE=" + legacyCase + ","
        + "ADDITIONAL_TRAVEL_PAYMENT=" + additionalTravelPayment + ","
        + "ELIGIBLE_CLIENT_INDICATOR=" + eligibleClientIndicator + ","
        + "IRC_SURGERY=" + ircSurgery + ","
        + "SUBSTANTIVE_HEARING=" + substantiveHearing + ","
        + "TOLERANCE_INDICATOR=" + toleranceIndicator + ","
        + "SURGERY_DATE=" + surgeryDate + ","
        + "REP_ORDER_DATE=" + repOrderDate + ","
        + "TRANSFER_DATE=" + transferDate + ","
        + "SCHEDULE_REF=" + data.scheduleRef;
  }

  // ==================== Utility Methods ====================

  private static String handleDateOverride(String override, String defaultValue) {
    if (override != null && !override.isBlank()) {
      return override;
    }
    return defaultValue;
  }

  private static LocalDate parseDateOrUseRandom(String dateString, LocalDate minDate, LocalDate maxDate) {
    if (dateString != null && !dateString.isBlank()) {
      try {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK));
      } catch (Exception e) {
        // If parsing fails, return a random date (the invalid string will be used elsewhere)
        return randomDateBetween(minDate, maxDate);
      }
    }
    return randomDateBetween(minDate, maxDate);
  }

  private static String buildUfn(LocalDate caseStart, int caseNum) {
    return padNum(caseStart.getDayOfMonth(), 2)
        + padNum(caseStart.getMonthValue(), 2)
        + (caseStart.getYear() % 100)
        + "/"
        + padNum(caseNum + 1, 3);
  }

  private static String buildUcn(LocalDate dob, String surname, String firstName) {
    return padNum(dob.getDayOfMonth(), 2)
        + padNum(dob.getMonthValue(), 2)
        + dob.getYear()
        + "/"
        + firstName.substring(0, 1).toUpperCase()
        + "/"
        + clean(surname).substring(0, Math.min(3, clean(surname).length()));
  }

  private static String clean(String s) {
    return s.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
  }

  private static String formatDate(LocalDate date) {
    return date.format(SLASH_DATE);
  }

  private static String handleWorkConcludedDateOverride(
      Map<String, String> override, LocalDate defaultDate, String submissionPeriod) {
    String workConcludedDateValue = override.getOrDefault("workConcludedDate", null);
    if (workConcludedDateValue == null) {
      return formatDate(defaultDate);
    }
    // Handle special "later" keyword - set to a date after the 20th of the month following submission period
    // This tests the validation error: "Case Concluded Date cannot be later than the 20th of the month following the submission period"
    if (workConcludedDateValue.toLowerCase(Locale.ROOT).contains("later")) {
      try {
        YearMonth period = YearMonth.parse(submissionPeriod, SHORT_PERIOD_PARSER);
        // For current/future submission periods, go to next year; for past periods, use current calculation
        LocalDate now = LocalDate.now();
        YearMonth currentPeriod = YearMonth.from(now);
        LocalDate targetMonth = period.atDay(1).plusMonths(1); // Month following submission period
        
        // If the target date is in the past, add a year to make it future (triggers validation)
        if (targetMonth.isBefore(now)) {
          targetMonth = targetMonth.plusYears(1);
        }
        
        LocalDate afterCutoff = targetMonth.withDayOfMonth(21); // 21st of month following submission period
        String formattedDate = formatDate(afterCutoff);
        System.out.println("[DEBUG] workConcludedDate 'later' override -> " + formattedDate + " (after 20th of following month)");
        return formattedDate;
      } catch (Exception e) {
        System.out.println("[DEBUG] Failed to parse submission period for 'later' override: " + e.getMessage());
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return formatDate(yesterday);
      }
    }
    // Otherwise use the provided date as-is
    return workConcludedDateValue;
  }

  private static String padNum(int num, int length) {
    return String.format("%0" + length + "d", num);
  }

  private static String randomPostCode() {
    // Generate UK-like postcode: XX00 0XX
    String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String digits = "0123456789";
    return "" + randomCharFrom(letters) + randomCharFrom(letters)
        + randomCharFrom(digits) + randomCharFrom(digits)
        + " " + randomCharFrom(digits)
        + randomCharFrom(letters) + randomCharFrom(letters);
  }

  private static double randomAmount(double min, double max) {
    return min + (RANDOM.nextDouble() * (max - min));
  }

  private static LocalDate randomDateBetween(LocalDate from, LocalDate to) {
    long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);
    long randomDays = RANDOM.nextLong(days + 1);
    return from.plusDays(randomDays);
  }

  private static <T> T randomFrom(List<T> list) {
    if (list.isEmpty()) {
      throw new IllegalArgumentException("List is empty");
    }
    return list.get(RANDOM.nextInt(list.size()));
  }

  private static String randomCharFrom(String s) {
    if (s.isEmpty()) {
      throw new IllegalArgumentException("String is empty");
    }
    return String.valueOf(s.charAt(RANDOM.nextInt(s.length())));
  }

  private static String extractOfficeOrDefault(Map<String, String> row) {
    String office = row.get("office");
    if (office != null && !office.isBlank()) {
      return office;
    }
    office = row.get("account");
    return office != null && !office.isBlank() ? office : randomFrom(OFFICES);
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

   // ==================== Data Class ====================

   private static String escapeXml(String value) {
     if (value == null) {
       return "";
     }
     return value
         .replace("&", "&amp;")
         .replace("<", "&lt;")
         .replace(">", "&gt;")
         .replace("\"", "&quot;")
         .replace("'", "&apos;");
   }

   private static String getDefaultSubmissionPeriod() {
     YearMonth lastMonth = YearMonth.now().minusMonths(1);
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
     return lastMonth.format(formatter).toUpperCase(Locale.ROOT);
   }

   private static class OutcomeData {
    String caseRefNumber, caseStartDate, caseId, ufn, procurementArea, accessPoint;
    String clientForename, clientSurname, clientDateOfBirth, ucn, gender, ethnicity, disability;
    String clientPostCode, workConcludedDate, caseStageLevel, adviceTime, travelTime, waitingTime;
    String profitCost, disbursementsAmount, counselCost, disbursementsVat, travelWaitingCosts;
    String vatIndicator, londonNonLondonRate, travelCosts, outcomeCode, postalApplAccp;
    String nationalRefMechanismAdvice, legacyCase, additionalTravelPayment;
    String eligibleClientIndicator, ircSurgery, substantiveHearing, toleranceIndicator;
    String surgeryDate, repOrderDate, transferDate, scheduleRef;

    OutcomeData caseRefNumber(String v) { this.caseRefNumber = v; return this; }
    OutcomeData caseStartDate(String v) { this.caseStartDate = v; return this; }
    OutcomeData caseId(String v) { this.caseId = v; return this; }
    OutcomeData ufn(String v) { this.ufn = v; return this; }
    OutcomeData procurementArea(String v) { this.procurementArea = v; return this; }
    OutcomeData accessPoint(String v) { this.accessPoint = v; return this; }
    OutcomeData clientForename(String v) { this.clientForename = v; return this; }
    OutcomeData clientSurname(String v) { this.clientSurname = v; return this; }
    OutcomeData clientDateOfBirth(String v) { this.clientDateOfBirth = v; return this; }
    OutcomeData ucn(String v) { this.ucn = v; return this; }
    OutcomeData gender(String v) { this.gender = v; return this; }
    OutcomeData ethnicity(String v) { this.ethnicity = v; return this; }
    OutcomeData disability(String v) { this.disability = v; return this; }
    OutcomeData clientPostCode(String v) { this.clientPostCode = v; return this; }
    OutcomeData workConcludedDate(String v) { this.workConcludedDate = v; return this; }
    OutcomeData caseStageLevel(String v) { this.caseStageLevel = v; return this; }
    OutcomeData adviceTime(String v) { this.adviceTime = v; return this; }
    OutcomeData travelTime(String v) { this.travelTime = v; return this; }
    OutcomeData waitingTime(String v) { this.waitingTime = v; return this; }
    OutcomeData profitCost(String v) { this.profitCost = v; return this; }
    OutcomeData disbursementsAmount(String v) { this.disbursementsAmount = v; return this; }
    OutcomeData counselCost(String v) { this.counselCost = v; return this; }
    OutcomeData disbursementsVat(String v) { this.disbursementsVat = v; return this; }
    OutcomeData travelWaitingCosts(String v) { this.travelWaitingCosts = v; return this; }
    OutcomeData vatIndicator(String v) { this.vatIndicator = v; return this; }
    OutcomeData londonNonLondonRate(String v) { this.londonNonLondonRate = v; return this; }
    OutcomeData travelCosts(String v) { this.travelCosts = v; return this; }
    OutcomeData outcomeCode(String v) { this.outcomeCode = v; return this; }
    OutcomeData postalApplAccp(String v) { this.postalApplAccp = v; return this; }
    OutcomeData nationalRefMechanismAdvice(String v) { this.nationalRefMechanismAdvice = v; return this; }
    OutcomeData legacyCase(String v) { this.legacyCase = v; return this; }
    OutcomeData additionalTravelPayment(String v) { this.additionalTravelPayment = v; return this; }
    OutcomeData eligibleClientIndicator(String v) { this.eligibleClientIndicator = v; return this; }
    OutcomeData ircSurgery(String v) { this.ircSurgery = v; return this; }
    OutcomeData substantiveHearing(String v) { this.substantiveHearing = v; return this; }
    OutcomeData toleranceIndicator(String v) { this.toleranceIndicator = v; return this; }
    OutcomeData surgeryDate(String v) { this.surgeryDate = v; return this; }
    OutcomeData repOrderDate(String v) { this.repOrderDate = v; return this; }
    OutcomeData transferDate(String v) { this.transferDate = v; return this; }
    OutcomeData scheduleRef(String v) { this.scheduleRef = v; return this; }
  }
}

