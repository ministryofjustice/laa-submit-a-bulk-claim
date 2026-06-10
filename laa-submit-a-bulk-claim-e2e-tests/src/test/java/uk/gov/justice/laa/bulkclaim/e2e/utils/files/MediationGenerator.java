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
 * Direct Java equivalent to generateMediationFiles.ts
 * Generates Mediation submission files with randomized, realistic data.
 */
public final class MediationGenerator {

  private static final DateTimeFormatter SLASH_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK);
  private static final DateTimeFormatter SHORT_PERIOD =
      DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter SHORT_PERIOD_PARSER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("MMM-yyyy")
          .toFormatter(Locale.ENGLISH);

  private static final List<String> OFFICES = List.of("0P322F", "1T102C", "2L848R");
  private static final List<String> FEE_CODES = List.of("ASSA");
  private static final LocalDate MIN_CASE_DATE = LocalDate.of(2015, 5, 1);

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  private MediationGenerator() {}

  /**
   * TS-like entrypoint parity:
   * GenerateMediationFiles(files, outcomes, format, options) => { filePaths, office }
   */
  public static GeneratorResult GenerateMediationFiles(
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
      String baseName = "mediation_" + suffix + "_" + i;
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
   * Generate a Mediation submission file with the specified number of outcomes.
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
   * Generate a Mediation submission file from a claims table (with optional overrides).
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

        SubmissionPeriodHelper.SubmissionPeriodResult period = null;
        try {
          // Get submission period - validate only the area of law, not individual fee codes
          period = SubmissionPeriodHelper.getUniqueSubmissionPeriod(office, "MEDIATION", null);
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
        throw new IOException("Failed to generate Mediation file: " + e.getMessage(), e);
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

      mapped.putAll(row);

      if (row.containsKey("netDisbursementAmount")) {
        mapped.put("disbursementsAmount", row.get("netDisbursementAmount"));
      }
      if (row.containsKey("disbursementVatAmount")) {
        mapped.put("disbursementsVat", row.get("disbursementVatAmount"));
      }
      if (row.containsKey("numberOfMediationSessions")) {
        mapped.put("numberOfMediationSessions", row.get("numberOfMediationSessions"));
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

    String mon = period.period().substring(0, 3).toUpperCase(Locale.ROOT);
    String yr = period.period().substring(period.period().length() - 2);
    String scheduleNum = office + "/MEDI" + mon + yr + "/01";

    sb.append("SCHEDULE,submissionPeriod=")
        .append(period.period())
        .append(",areaOfLaw=MEDIATION,scheduleNum=")
        .append(scheduleNum)
        .append("\n");

    LocalDate scheduleStart =
        period.scheduleStart() != null ? LocalDate.parse(period.scheduleStart()) : MIN_CASE_DATE;
    LocalDate scheduleEnd =
        period.scheduleEnd() != null ? LocalDate.parse(period.scheduleEnd()) : LocalDate.now().plusYears(3);

    for (int i = 0; i < claimRows.size(); i++) {
      Map<String, String> override = claimRows.get(i);
      OutcomeData outcome =
          generateOutcome(office, i, scheduleStart, scheduleEnd, period.period(), override, scheduleNum);
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

     String mon = period.period().substring(0, 3).toUpperCase(Locale.ROOT);
     String yr = period.period().substring(period.period().length() - 2);
     String scheduleNum = office + "/MEDI" + mon + yr + "/01";

     sb.append("    <schedule submissionPeriod=\"")
         .append(escapeXml(period.period()))
         .append("\" areaOfLaw=\"MEDIATION\" scheduleNum=\"")
         .append(escapeXml(scheduleNum))
         .append("\">\n");

     LocalDate scheduleStart =
         period.scheduleStart() != null ? LocalDate.parse(period.scheduleStart()) : MIN_CASE_DATE;
     LocalDate scheduleEnd =
         period.scheduleEnd() != null ? LocalDate.parse(period.scheduleEnd()) : LocalDate.now().plusYears(3);

     for (int i = 0; i < claimRows.size(); i++) {
       Map<String, String> override = claimRows.get(i);
       OutcomeData outcome =
            generateOutcome(office, i, scheduleStart, scheduleEnd, period.period(), override, scheduleNum);
       String matterType = override.getOrDefault("matterType", "MEDI:MDCS");
       sb.append("      <outcome matterType=\"").append(escapeXml(matterType)).append("\">\n");

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
     fields.put("CASE_START_DATE", data.caseStartDate);
     fields.put("CASE_ID", data.caseId);
     fields.put("UFN", data.ufn);
     fields.put("CLIENT_FORENAME", data.client1Forename);
     fields.put("CLIENT_SURNAME", data.client1Surname);
     fields.put("CLIENT_DATE_OF_BIRTH", data.client1DateOfBirth);
     fields.put("UCN", data.ucn1);
     fields.put("GENDER", data.client1Gender);
     fields.put("ETHNICITY", data.client1Ethnicity);
     fields.put("DISABILITY", data.client1Disability);
     fields.put("CLIENT_POST_CODE", data.client1PostCode);
     fields.put("CLIENT_LEGALLY_AIDED", data.client1LegallyAided);
     fields.put("CLIENT2_FORENAME", data.client2Forename);
     fields.put("CLIENT2_SURNAME", data.client2Surname);
     fields.put("CLIENT2_DATE_OF_BIRTH", data.client2DateOfBirth);
     fields.put("CLIENT2_UCN", data.ucn2);
     fields.put("CLIENT2_GENDER", data.client2Gender);
     fields.put("CLIENT2_ETHNICITY", data.client2Ethnicity);
     fields.put("CLIENT2_DISABILITY", data.client2Disability);
     fields.put("CLIENT2_POST_CODE", data.client2PostCode);
     fields.put("CLIENT2_LEGALLY_AIDED", data.client2LegallyAided);
     fields.put("MED_CONCLUDED_DATE", data.medConcludedDate);
     fields.put("WORK_CONCLUDED_DATE", data.workConcludedDate);
     fields.put("NUMBER_OF_MEDIATION_SESSIONS", data.numberOfMediationSessions);
     fields.put("MEDIATION_TIME", data.mediationTime);
     fields.put("CASE_REF_NUMBER", data.caseRefNumber);
     fields.put("OUTCOME_CODE", data.outcomeCode);
     fields.put("DISBURSEMENTS_AMOUNT", data.disbursementsAmount);
     fields.put("DISBURSEMENTS_VAT", data.disbursementsVat);
     fields.put("VAT_INDICATOR", data.vatIndicator);
     fields.put("UNIQUE_CASE_ID", data.uniqueCaseId);
     fields.put("OUTREACH", data.outreach);
     fields.put("REFERRAL", data.referral);
     fields.put("POSTAL_APPL_ACCP", data.client1PostalApplAccp);
     fields.put("CLIENT2_POSTAL_APPL_ACCP", data.client2PostalApplAccp);
     fields.put("SCHEDULE_REF", data.scheduleRef);
     fields.put("NATIONAL_REF_MECHANISM_ADVICE", data.nrmAdvice);
     fields.put("LEGACY_CASE", data.legacyCase);
     fields.put("LONDON_NONLONDON_RATE", data.londonNonLondonRate);
     fields.put("ADDITIONAL_TRAVEL_PAYMENT", data.additionalTravelPayment);
     fields.put("ELIGIBLE_CLIENT_INDICATOR", data.eligibleClientIndicator);
     fields.put("IRC_SURGERY", data.ircSurgery);
     fields.put("SUBSTANTIVE_HEARING", data.substantiveHearing);
     fields.put("TOLERANCE_INDICATOR", data.toleranceIndicator);
     fields.put("DUTY_SOLICITOR", data.dutySolicitor);
     fields.put("YOUTH_COURT", data.youthCourt);
     return fields;
   }

   private static OutcomeData generateOutcome(
       String office,
       int caseNum,
       LocalDate scheduleStart,
       LocalDate scheduleEnd,
       String submissionPeriod,
       Map<String, String> override,
       String scheduleNum)
       throws Exception {
    String client1First = override.getOrDefault("client1First", "Client");
    String client1Last = override.getOrDefault("client1Last", "One" + (caseNum + 1));
    String client2First = override.getOrDefault("client2First", "Client");
    String client2Last = override.getOrDefault("client2Last", "Two" + (caseNum + 1));

    LocalDate start = scheduleStart != null ? scheduleStart : MIN_CASE_DATE;
    LocalDate end = scheduleEnd != null ? scheduleEnd : LocalDate.now().plusYears(3);

     // Adjust end to 20th of month following submission period
     // This ensures case concluded date doesn't exceed 20th of month following submission period
     YearMonth period = YearMonth.parse(submissionPeriod, SHORT_PERIOD_PARSER);
     end = period.plusMonths(1).atDay(20);

    // Ensure valid range
    if (end.isBefore(start)) {
      end = start.plusMonths(1);
    }

    LocalDate caseStart = randomDateBetween(start, end);
    LocalDate medConcluded = randomDateBetween(caseStart, end);
    LocalDate workConcluded = randomDateBetween(caseStart, medConcluded);

    LocalDate dob1 = randomDateBetween(LocalDate.of(1950, 1, 1), LocalDate.of(2000, 12, 31));
    LocalDate dob2 = randomDateBetween(LocalDate.of(1950, 1, 1), LocalDate.of(2000, 12, 31));

    String ufn = override.getOrDefault("ufn", buildUfn(caseStart, caseNum));
    String ucn1 = override.getOrDefault("ucn", buildUcn(dob1, client1Last));
    String ucn2 = override.getOrDefault("ucn2", buildUcn(dob2, client2Last));

    return new OutcomeData()
        .caseRefNumber(
            override.getOrDefault("caseRefNumber", String.valueOf(1000 + caseNum + 1)))
        .caseStartDate(
            override.getOrDefault("caseStartDate", formatDate(caseStart)))
        .caseId(padNum(caseNum + 1, 3))
        .ufn(ufn)
        .client1Forename(client1First)
        .client1Surname(client1Last)
        .client1DateOfBirth(
            override.getOrDefault("client1DateOfBirth", formatDate(dob1)))
        .ucn1(ucn1)
        .client1Gender(override.getOrDefault("client1Gender", randomFrom(List.of("M", "F"))))
        .client1Ethnicity(override.getOrDefault("client1Ethnicity", "01"))
        .client1Disability(
            override.getOrDefault("client1Disability", randomFrom(List.of("NCD", "ILL"))))
        .client1PostCode(
            override.getOrDefault("client1PostCode", randomPostCode()))
        .client1LegallyAided(
            override.getOrDefault("client1LegallyAided", randomFrom(List.of("Y", "N"))))
        .client2Forename(client2First)
        .client2Surname(client2Last)
        .client2DateOfBirth(
            override.getOrDefault("client2DateOfBirth", formatDate(dob2)))
        .ucn2(ucn2)
        .client2Gender(override.getOrDefault("client2Gender", randomFrom(List.of("M", "F"))))
        .client2Ethnicity(override.getOrDefault("client2Ethnicity", "01"))
        .client2Disability(
            override.getOrDefault("client2Disability", randomFrom(List.of("NCD", "ILL"))))
        .client2PostCode(
            override.getOrDefault("client2PostCode", randomPostCode()))
        .client2LegallyAided(
            override.getOrDefault("client2LegallyAided", randomFrom(List.of("Y", "N"))))
        .medConcludedDate(
            override.getOrDefault("medConcludedDate", formatDate(medConcluded)))
        .workConcludedDate(
            override.getOrDefault("workConcludedDate", formatDate(workConcluded)))
        .numberOfMediationSessions(
            override.getOrDefault(
                "numberOfMediationSessions", String.valueOf(randomIntBetween(1, 5))))
        .mediationTime(
            override.getOrDefault("mediationTime", String.valueOf(randomIntBetween(60, 240))))
        .outcomeCode(override.getOrDefault("outcomeCode", "B"))
        .disbursementsAmount(
            override.getOrDefault(
                "disbursementsAmount", String.format("%.2f", randomAmount(0, 200))))
        .disbursementsVat(
            override.getOrDefault(
                "disbursementsVat", String.format("%.2f", randomAmount(0, 50))))
        .vatIndicator(
            override.getOrDefault("vatIndicator", randomFrom(List.of("Y", "N"))))
        .uniqueCaseId(ufn)
        .outreach(
            override.getOrDefault("outreach", randomFrom(List.of("000", "001", "002"))))
        .referral(
            override.getOrDefault("referral", randomFrom(List.of("08", "09", "10"))))
        .client1PostalApplAccp(override.getOrDefault("client1PostalApplAccp", "Y"))
        .client2PostalApplAccp(
            override.getOrDefault("client2PostalApplAccp", randomFrom(List.of("Y", "N"))))
        .scheduleRef(scheduleNum)
        .nrmAdvice(override.getOrDefault("nrmAdvice", randomFrom(List.of("Y", "N"))))
        .legacyCase(override.getOrDefault("legacyCase", randomFrom(List.of("Y", "N"))))
        .londonNonLondonRate(
            override.getOrDefault("londonNonLondonRate", randomFrom(List.of("Y", "N"))))
        .additionalTravelPayment(
            override.getOrDefault("additionalTravelPayment", randomFrom(List.of("Y", "N"))))
        .eligibleClientIndicator(
            override.getOrDefault("eligibleClientIndicator", randomFrom(List.of("Y", "N"))))
        .ircSurgery(
            override.getOrDefault("ircSurgery", randomFrom(List.of("Y", "N"))))
        .substantiveHearing(
            override.getOrDefault("substantiveHearing", randomFrom(List.of("Y", "N"))))
        .toleranceIndicator(
            override.getOrDefault("toleranceIndicator", randomFrom(List.of("Y", "N"))))
        .dutySolicitor(
            override.getOrDefault("dutySolicitor", randomFrom(List.of("Y", "N"))))
        .youthCourt(override.getOrDefault("youthCourt", randomFrom(List.of("Y", "N"))));
  }

  private static String formatOutcome(OutcomeData data, Map<String, String> override) {
    String feeCode = override.getOrDefault("feeCode", randomFrom(FEE_CODES));

    return "OUTCOME,"
        + "FEE_CODE=" + feeCode + ","
        + "matterType=MEDI:MDCS,"
        + "CASE_START_DATE=" + data.caseStartDate + ","
        + "CASE_ID=" + data.caseId + ","
        + "UFN=" + data.ufn + ","
        + "CLIENT_FORENAME=" + data.client1Forename + ","
        + "CLIENT_SURNAME=" + data.client1Surname + ","
        + "CLIENT_DATE_OF_BIRTH=" + data.client1DateOfBirth + ","
        + "UCN=" + data.ucn1 + ","
        + "GENDER=" + data.client1Gender + ","
        + "ETHNICITY=" + data.client1Ethnicity + ","
        + "DISABILITY=" + data.client1Disability + ","
        + "CLIENT_POST_CODE=" + data.client1PostCode + ","
        + "CLIENT_LEGALLY_AIDED=" + data.client1LegallyAided + ","
        + "CLIENT2_FORENAME=" + data.client2Forename + ","
        + "CLIENT2_SURNAME=" + data.client2Surname + ","
        + "CLIENT2_DATE_OF_BIRTH=" + data.client2DateOfBirth + ","
        + "CLIENT2_UCN=" + data.ucn2 + ","
        + "CLIENT2_GENDER=" + data.client2Gender + ","
        + "CLIENT2_ETHNICITY=" + data.client2Ethnicity + ","
        + "CLIENT2_DISABILITY=" + data.client2Disability + ","
        + "CLIENT2_POST_CODE=" + data.client2PostCode + ","
        + "CLIENT2_LEGALLY_AIDED=" + data.client2LegallyAided + ","
        + "MED_CONCLUDED_DATE=" + data.medConcludedDate + ","
        + "WORK_CONCLUDED_DATE=" + data.workConcludedDate + ","
        + "NUMBER_OF_MEDIATION_SESSIONS=" + data.numberOfMediationSessions + ","
        + "MEDIATION_TIME=" + data.mediationTime + ","
        + "CASE_REF_NUMBER=" + data.caseRefNumber + ","
        + "OUTCOME_CODE=" + data.outcomeCode + ","
        + "DISBURSEMENTS_AMOUNT=" + data.disbursementsAmount + ","
        + "DISBURSEMENTS_VAT=" + data.disbursementsVat + ","
        + "VAT_INDICATOR=" + data.vatIndicator + ","
        + "UNIQUE_CASE_ID=" + data.uniqueCaseId + ","
        + "OUTREACH=" + data.outreach + ","
        + "REFERRAL=" + data.referral + ","
        + "POSTAL_APPL_ACCP=" + data.client1PostalApplAccp + ","
        + "CLIENT2_POSTAL_APPL_ACCP=" + data.client2PostalApplAccp + ","
        + "SCHEDULE_REF=" + data.scheduleRef + ","
        + "NATIONAL_REF_MECHANISM_ADVICE=" + data.nrmAdvice + ","
        + "LEGACY_CASE=" + data.legacyCase + ","
        + "LONDON_NONLONDON_RATE=" + data.londonNonLondonRate + ","
        + "ADDITIONAL_TRAVEL_PAYMENT=" + data.additionalTravelPayment + ","
        + "ELIGIBLE_CLIENT_INDICATOR=" + data.eligibleClientIndicator + ","
        + "IRC_SURGERY=" + data.ircSurgery + ","
        + "SUBSTANTIVE_HEARING=" + data.substantiveHearing + ","
        + "TOLERANCE_INDICATOR=" + data.toleranceIndicator + ","
        + "DUTY_SOLICITOR=" + data.dutySolicitor + ","
        + "YOUTH_COURT=" + data.youthCourt;
  }

  // ==================== Utility Methods ====================

  private static String buildUfn(LocalDate caseStart, int caseNum) {
    return padNum(caseStart.getDayOfMonth(), 2)
        + padNum(caseStart.getMonthValue(), 2)
        + (caseStart.getYear() % 100)
        + "/"
        + padNum(caseNum + 1, 3);
  }

  private static String buildUcn(LocalDate dob, String surname) {
    return padNum(dob.getDayOfMonth(), 2)
        + padNum(dob.getMonthValue(), 2)
        + dob.getYear()
        + "/"
        + surname.substring(0, 1).toUpperCase()
        + "/"
        + surname.substring(0, Math.min(4, surname.length())).toUpperCase();
  }

  private static String formatDate(LocalDate date) {
    return date.format(SLASH_DATE);
  }

   private static String padNum(int num, int length) {
     return String.format("%0" + length + "d", num);
   }

   private static String getDefaultSubmissionPeriod() {
     YearMonth lastMonth = YearMonth.now().minusMonths(1);
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
     return lastMonth.format(formatter).toUpperCase(Locale.ROOT);
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

  private static int randomIntBetween(int min, int max) {
    return min + RANDOM.nextInt(max - min + 1);
  }

  private static LocalDate randomDateBetween(LocalDate from, LocalDate to) {
    long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);
    long randomDays = days > 0 ? RANDOM.nextLong(days + 1) : 0;
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

   private static class OutcomeData {
    String caseRefNumber, caseStartDate, caseId, ufn;
    String client1Forename, client1Surname, client1DateOfBirth, ucn1, client1Gender;
    String client1Ethnicity, client1Disability, client1PostCode, client1LegallyAided;
    String client2Forename, client2Surname, client2DateOfBirth, ucn2, client2Gender;
    String client2Ethnicity, client2Disability, client2PostCode, client2LegallyAided;
    String medConcludedDate, workConcludedDate, numberOfMediationSessions, mediationTime;
    String outcomeCode, disbursementsAmount, disbursementsVat, vatIndicator, uniqueCaseId;
    String outreach, referral, client1PostalApplAccp, client2PostalApplAccp, scheduleRef;
    String nrmAdvice, legacyCase, londonNonLondonRate, additionalTravelPayment;
    String eligibleClientIndicator, ircSurgery, substantiveHearing, toleranceIndicator;
    String dutySolicitor, youthCourt;

    OutcomeData caseRefNumber(String v) { this.caseRefNumber = v; return this; }
    OutcomeData caseStartDate(String v) { this.caseStartDate = v; return this; }
    OutcomeData caseId(String v) { this.caseId = v; return this; }
    OutcomeData ufn(String v) { this.ufn = v; return this; }
    OutcomeData client1Forename(String v) { this.client1Forename = v; return this; }
    OutcomeData client1Surname(String v) { this.client1Surname = v; return this; }
    OutcomeData client1DateOfBirth(String v) { this.client1DateOfBirth = v; return this; }
    OutcomeData ucn1(String v) { this.ucn1 = v; return this; }
    OutcomeData client1Gender(String v) { this.client1Gender = v; return this; }
    OutcomeData client1Ethnicity(String v) { this.client1Ethnicity = v; return this; }
    OutcomeData client1Disability(String v) { this.client1Disability = v; return this; }
    OutcomeData client1PostCode(String v) { this.client1PostCode = v; return this; }
    OutcomeData client1LegallyAided(String v) { this.client1LegallyAided = v; return this; }
    OutcomeData client2Forename(String v) { this.client2Forename = v; return this; }
    OutcomeData client2Surname(String v) { this.client2Surname = v; return this; }
    OutcomeData client2DateOfBirth(String v) { this.client2DateOfBirth = v; return this; }
    OutcomeData ucn2(String v) { this.ucn2 = v; return this; }
    OutcomeData client2Gender(String v) { this.client2Gender = v; return this; }
    OutcomeData client2Ethnicity(String v) { this.client2Ethnicity = v; return this; }
    OutcomeData client2Disability(String v) { this.client2Disability = v; return this; }
    OutcomeData client2PostCode(String v) { this.client2PostCode = v; return this; }
    OutcomeData client2LegallyAided(String v) { this.client2LegallyAided = v; return this; }
    OutcomeData medConcludedDate(String v) { this.medConcludedDate = v; return this; }
    OutcomeData workConcludedDate(String v) { this.workConcludedDate = v; return this; }
    OutcomeData numberOfMediationSessions(String v) { this.numberOfMediationSessions = v; return this; }
    OutcomeData mediationTime(String v) { this.mediationTime = v; return this; }
    OutcomeData outcomeCode(String v) { this.outcomeCode = v; return this; }
    OutcomeData disbursementsAmount(String v) { this.disbursementsAmount = v; return this; }
    OutcomeData disbursementsVat(String v) { this.disbursementsVat = v; return this; }
    OutcomeData vatIndicator(String v) { this.vatIndicator = v; return this; }
    OutcomeData uniqueCaseId(String v) { this.uniqueCaseId = v; return this; }
    OutcomeData outreach(String v) { this.outreach = v; return this; }
    OutcomeData referral(String v) { this.referral = v; return this; }
    OutcomeData client1PostalApplAccp(String v) { this.client1PostalApplAccp = v; return this; }
    OutcomeData client2PostalApplAccp(String v) { this.client2PostalApplAccp = v; return this; }
    OutcomeData scheduleRef(String v) { this.scheduleRef = v; return this; }
    OutcomeData nrmAdvice(String v) { this.nrmAdvice = v; return this; }
    OutcomeData legacyCase(String v) { this.legacyCase = v; return this; }
    OutcomeData londonNonLondonRate(String v) { this.londonNonLondonRate = v; return this; }
    OutcomeData additionalTravelPayment(String v) { this.additionalTravelPayment = v; return this; }
    OutcomeData eligibleClientIndicator(String v) { this.eligibleClientIndicator = v; return this; }
    OutcomeData ircSurgery(String v) { this.ircSurgery = v; return this; }
    OutcomeData substantiveHearing(String v) { this.substantiveHearing = v; return this; }
    OutcomeData toleranceIndicator(String v) { this.toleranceIndicator = v; return this; }
    OutcomeData dutySolicitor(String v) { this.dutySolicitor = v; return this; }
    OutcomeData youthCourt(String v) { this.youthCourt = v; return this; }
  }
}

