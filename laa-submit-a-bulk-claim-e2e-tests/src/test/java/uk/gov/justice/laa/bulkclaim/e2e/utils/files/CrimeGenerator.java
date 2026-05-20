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
 * Direct Java equivalent to generateCrimeFiles.ts
 * Generates Crime Lower submission files with randomized, realistic data.
 */
public final class CrimeGenerator {

  private static final DateTimeFormatter SLASH_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK);
  private static final DateTimeFormatter SHORT_PERIOD =
      DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter SHORT_PERIOD_PARSER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("MMM-yyyy")
          .toFormatter(Locale.ENGLISH);

  private static final List<String> OFFICES = List.of("0P322F", "1T102C", "2L848R");
  private static final List<String> FEE_CODES = List.of("APPA", "APPB");
  private static final LocalDate MIN_CASE_DATE = LocalDate.of(2015, 4, 1);

  // Police stations with mapped scheme IDs (matching TS)
  private static final List<PoliceStation> POLICE_STATIONS = List.of(
      new PoliceStation("NE001", 1001),
      new PoliceStation("NE900", 1001),
      new PoliceStation("NE002", 1002),
      new PoliceStation("NE003", 1002),
      new PoliceStation("RD001", 1131),
      new PoliceStation("RD002", 1131),
      new PoliceStation("RD003", 1131),
      new PoliceStation("RD016", 1134));

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  private CrimeGenerator() {}

  /**
   * TS-like entrypoint parity:
   * GenerateCrimeFiles(files, outcomes, format, options) => { filePaths, office }
   */
  public static GeneratorResult GenerateCrimeFiles(
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
      String baseName = "crime_" + suffix + "_" + i;
      String normalized = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
      String intermediate = "xml".equals(normalized) ? "csv" : normalized;

      Path out = Paths.get("build", "tmp", "e2e", "generated", baseName + "." + intermediate);
      List<Map<String, String>> claims =
          options != null && options.getClaims() != null ? options.getClaims() : List.of();

      Path generated = generateFromClaimsTable(intermediate, claims.isEmpty() ? defaultClaimRows(outcomes) : claims, out, office);
      if ("xml".equals(normalized)) {
        // Keep parity signature; XML conversion is handled by higher-level utilities if needed.
        paths.add(generated.toString());
      } else {
        paths.add(generated.toString());
      }
    }

    return new GeneratorResult(paths, office);
  }

  /**
   * Generate a Crime submission file with the specified number of outcomes.
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
   * Generate a Crime submission file from a claims table (with optional overrides).
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

        String preferredFeeCode = extractPreferredCrimeFeeCode(normalizedClaimRows);
        String selectedOffice = office;
        SubmissionPeriodHelper.SubmissionPeriodResult period =
            resolveCrimeSubmissionPeriod(selectedOffice, preferredFeeCode);

        if (period == null) {
          for (String candidateOffice : OFFICES) {
            if (candidateOffice.equals(selectedOffice)) {
              continue;
            }
            period = resolveCrimeSubmissionPeriod(candidateOffice, preferredFeeCode);
            if (period != null) {
              selectedOffice = candidateOffice;
              break;
            }
          }
        }

        if (period == null) {
          throw new IllegalStateException(
              "Unable to resolve unique Crime submission period for offices " + OFFICES);
        }

        String content = "xml".equalsIgnoreCase(format)
            ? buildXmlFileContent(selectedOffice, period, normalizedClaimRows)
            : buildFileContent(selectedOffice, period, normalizedClaimRows);

        Files.writeString(filePath, content, StandardCharsets.UTF_8);
        waitForFile(filePath, 5000);
        return filePath;
      } catch (Exception e) {
        throw new IOException("Failed to generate Crime file: " + e.getMessage(), e);
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

      if (row.containsKey("netProfitCosts")) {
        mapped.put("profitCost", row.get("netProfitCosts"));
      }
      if (row.containsKey("netTravelCosts")) {
        mapped.put("travelCosts", row.get("netTravelCosts"));
      }
      if (row.containsKey("netWaitingCosts")) {
        mapped.put("travelWaitingCosts", row.get("netWaitingCosts"));
      }
      if (row.containsKey("netDisbursementAmount")) {
        mapped.put("disbursementsAmount", row.get("netDisbursementAmount"));
      }
      if (row.containsKey("disbursementVatAmount")) {
        mapped.put("disbursementsVat", row.get("disbursementVatAmount"));
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

    private static SubmissionPeriodHelper.SubmissionPeriodResult resolveCrimeSubmissionPeriod(
        String office,
        String feeCode) {
      Exception lastError = null;

      for (int attempt = 0; attempt < 3; attempt++) {
        try {
          return SubmissionPeriodHelper.getUniqueSubmissionPeriod(office, "CRIME LOWER", feeCode);
        } catch (Exception e) {
          lastError = e;
          if (attempt < 2) {
            try {
              Thread.sleep(200L * (attempt + 1));
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        }
      }

      // Secondary fallback: allow area-level contract selection if fee lookup endpoint is transiently unavailable.
      try {
        return SubmissionPeriodHelper.getUniqueSubmissionPeriod(office, "CRIME LOWER", null);
      } catch (Exception ignored) {
        if (lastError != null) {
          System.out.println(
              "[CRIME-GENERATOR] Failed to resolve period for office "
                  + office
                  + " fee="
                  + feeCode
                  + " cause="
                  + lastError.getMessage());
        }
        return null;
      }
    }

    private static String extractPreferredCrimeFeeCode(List<Map<String, String>> claimRows) {
      for (Map<String, String> row : claimRows) {
        if (row == null) {
          continue;
        }
        String feeCode = row.get("feeCode");
        if (feeCode == null || feeCode.isBlank()) {
          feeCode = row.get("FEE_CODE");
        }
        if (feeCode != null && !feeCode.isBlank()) {
          return feeCode;
        }
      }
      return "APPA";
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
         .append(",areaOfLaw=CRIME LOWER,scheduleNum=")
         .append(office)
         .append("/CRM\n");

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
          .append("\" areaOfLaw=\"CRIME LOWER\" scheduleNum=\"")
          .append(escapeXml(office))
          .append("/CRM\">\n");

      LocalDate scheduleStart =
          period.scheduleStart() != null ? LocalDate.parse(period.scheduleStart()) : MIN_CASE_DATE;
      LocalDate scheduleEnd =
          period.scheduleEnd() != null ? LocalDate.parse(period.scheduleEnd()) : LocalDate.now();

      for (int i = 0; i < claimRows.size(); i++) {
        Map<String, String> override = claimRows.get(i);
        OutcomeData outcome =
            generateOutcome(office, i, scheduleStart, scheduleEnd, period.period(), override);
        String feeCodeForMatterType = override.getOrDefault("feeCode", "APPA");
        String matterType =
            override.getOrDefault(
                "matterType",
                feeCodeForMatterType.substring(0, Math.min(4, feeCodeForMatterType.length())));
        sb.append("      <outcome matterType=\"").append(escapeXml(matterType)).append("\">\n");

        // Get all outcome fields and format them as XML items
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
      fields.put("UFN", data.ufn);
      fields.put("CLIENT_FORENAME", data.clientForename);
      fields.put("CLIENT_SURNAME", data.clientSurname);
      fields.put("CLIENT_DATE_OF_BIRTH", data.clientDateOfBirth);
      fields.put("GENDER", data.gender);
      fields.put("ETHNICITY", data.ethnicity);
      fields.put("DISABILITY", data.disability);
      fields.put("CASE_START_DATE", data.caseStartDate);
      fields.put("PROFIT_COST", data.profitCost);
      fields.put("DISBURSEMENTS_AMOUNT", data.disbursementsAmount);
      fields.put("DISBURSEMENTS_VAT", data.disbursementsVat);
      fields.put("VAT_INDICATOR", data.vatIndicator);
      fields.put("TRAVEL_COSTS", data.travelCosts);
      fields.put("OUTCOME_CODE", data.outcomeCode);
      fields.put("CRIME_MATTER_TYPE", data.crimeMatterType);
      fields.put("TRAVEL_WAITING_COSTS", data.travelWaitingCosts);
      fields.put("WORK_CONCLUDED_DATE", data.workConcludedDate);
      fields.put("NO_OF_SUSPECTS", data.noOfSuspects);
      fields.put("NO_OF_POLICE_STATION", data.noOfPoliceStation);
      fields.put("POLICE_STATION", data.policeStation);
      fields.put("DUTY_SOLICITOR", data.dutySolicitor);
      fields.put("YOUTH_COURT", data.youthCourt);
      fields.put("SCHEME_ID", data.schemeId);
      fields.put("DSCC_NUMBER", data.dsccNumber);
      fields.put("POSTAL_APPL_ACCP", data.postalApplicationAccepted);
      fields.put("NATIONAL_REF_MECHANISM_ADVICE", data.nrmAdvice);
      fields.put("LEGACY_CASE", data.legacyCase);
      fields.put("LONDON_NONLONDON_RATE", data.londonNonLondonRate);
      fields.put("ADDITIONAL_TRAVEL_PAYMENT", data.additionalTravelPayment);
      fields.put("ELIGIBLE_CLIENT_INDICATOR", data.eligibleClientIndicator);
      fields.put("IRC_SURGERY", data.ircSurgery);
      fields.put("SUBSTANTIVE_HEARING", data.substantiveHearing);
      fields.put("TOLERANCE_INDICATOR", data.toleranceIndicator);
      fields.put("REP_ORDER_DATE", data.repOrderDate);
      fields.put("TRANSFER_DATE", data.transferDate);
      fields.put("SURGERY_DATE", data.surgeryDate);
      fields.put("CLIENT_LEGALLY_AIDED", data.clientLegallyAided);
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
      String firstName = override.getOrDefault("firstName", "Crime");
      String lastName = override.getOrDefault("lastName", "User" + (caseNum + 1));

      LocalDate start = scheduleStart != null ? scheduleStart : MIN_CASE_DATE;
      LocalDate end = scheduleEnd != null ? scheduleEnd : LocalDate.now();

       // Adjust end to 20th of month following submission period
       // This ensures case concluded date doesn't exceed 20th of month following submission period
       YearMonth period = YearMonth.parse(submissionPeriod, SHORT_PERIOD_PARSER);
       end = period.plusMonths(1).atDay(20);
       if (end.isAfter(LocalDate.now())) {
         end = LocalDate.now();
       }

       // Ensure valid range
        if (end.isBefore(start)) {
          start = end;
        }

      LocalDate caseStart = randomDateBetween(start, end);
      LocalDate concluded = randomDateBetween(caseStart, end);
      LocalDate dob = randomDateBetween(LocalDate.of(1960, 1, 1), LocalDate.of(2005, 12, 31));
      PoliceStation station = randomFrom(POLICE_STATIONS);

      String ufn = override.getOrDefault("ufn", buildUfn(caseStart, caseNum));

      return new OutcomeData()
          .clientForename(firstName)
          .clientSurname(lastName)
          .clientDateOfBirth(
              override.getOrDefault("clientDateOfBirth", formatDate(dob)))
          .gender(override.getOrDefault("gender", randomFrom(List.of("M", "F"))))
          .ethnicity(
              override.getOrDefault("ethnicity", randomFrom(List.of("99", "01", "02", "03", "04"))))
          .disability(
              override.getOrDefault(
                  "disability",
                  randomFrom(
                      List.of("NCD", "MOB", "DEA", "HEA", "VIS", "BLI", "MHC", "LDD", "COG", "ILL", "OTH",
                          "UKN", "PHY", "SEN"))))
          .caseStartDate(
              override.getOrDefault("caseStartDate", formatDate(caseStart)))
          .profitCost(
              override.getOrDefault("profitCost", String.format("%.2f", randomAmount(10, 200))))
          .disbursementsAmount(
              override.getOrDefault(
                  "disbursementsAmount", String.format("%.2f", randomAmount(0, 50))))
          .disbursementsVat(
              override.getOrDefault(
                  "disbursementsVat", String.format("%.2f", randomAmount(0, 1.98))))
          .vatIndicator(
              override.getOrDefault("vatIndicator", randomFrom(List.of("Y", "N"))))
          .travelCosts(
              override.getOrDefault("travelCosts", String.format("%.2f", randomAmount(0, 100))))
          .outcomeCode(
              override.getOrDefault(
                  "outcomeCode", randomFrom(List.of("CN04", "CN02", "CN01", "CN08"))))
          .crimeMatterType(
              override.getOrDefault(
                  "crimeMatterType", padNum(randomIntBetween(1, 3), 2)))
          .travelWaitingCosts(
              override.getOrDefault("travelWaitingCosts", String.format("%.2f", randomAmount(0, 40))))
          .workConcludedDate(
              override.getOrDefault("workConcludedDate", formatDate(concluded)))
          .noOfSuspects(
              override.getOrDefault("noOfSuspects", String.valueOf(randomIntBetween(1, 3))))
          .noOfPoliceStation(override.getOrDefault("noOfPoliceStation", "1"))
          .policeStation(
              override.getOrDefault("policeStation", station.id))
          .dutySolicitor(
              override.getOrDefault("dutySolicitor", randomFrom(List.of("Y", "N"))))
          .youthCourt(
              override.getOrDefault("youthCourt", randomFrom(List.of("Y", "N"))))
          .schemeId(
              override.getOrDefault("schemeId", String.valueOf(station.schemeId)))
          .dsccNumber(
              override.getOrDefault("dsccNumber", generateDscc()))
          .postalApplicationAccepted(
              override.getOrDefault("postalApplication", randomFrom(List.of("Y", "N"))))
          .nrmAdvice(
              override.getOrDefault("nrmAdvice", randomFrom(List.of("Y", "N"))))
          .legacyCase(
              override.getOrDefault("legacyCase", randomFrom(List.of("Y", "N"))))
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
          .repOrderDate(
              override.getOrDefault("repOrderDate", formatDate(caseStart)))
          .transferDate(
              override.getOrDefault("transferDate", formatDate(concluded)))
           .surgeryDate(
               override.getOrDefault("surgeryDate", formatDate(concluded)))
           .clientLegallyAided(
               override.getOrDefault("clientLegallyAided", randomFrom(List.of("Y", "N"))))
           .ufn(ufn);
     }

   private static String formatOutcome(OutcomeData data, Map<String, String> override) {
     String feeCode = override.getOrDefault("feeCode", randomFrom(FEE_CODES));
     String matterType = feeCode.substring(0, Math.min(4, feeCode.length()));

     return "OUTCOME,"
         + "FEE_CODE=" + feeCode + ","
         + "matterType=" + matterType + ","
         + "UFN=" + data.ufn + ","
         + "CLIENT_FORENAME=" + data.clientForename + ","
         + "CLIENT_SURNAME=" + data.clientSurname + ","
         + "CLIENT_DATE_OF_BIRTH=" + data.clientDateOfBirth + ","
         + "GENDER=" + data.gender + ","
         + "ETHNICITY=" + data.ethnicity + ","
         + "DISABILITY=" + data.disability + ","
         + "CASE_START_DATE=" + data.caseStartDate + ","
         + "PROFIT_COST=" + data.profitCost + ","
         + "DISBURSEMENTS_AMOUNT=" + data.disbursementsAmount + ","
         + "DISBURSEMENTS_VAT=" + data.disbursementsVat + ","
         + "VAT_INDICATOR=" + data.vatIndicator + ","
         + "TRAVEL_COSTS=" + data.travelCosts + ","
         + "OUTCOME_CODE=" + data.outcomeCode + ","
         + "CRIME_MATTER_TYPE=" + data.crimeMatterType + ","
         + "TRAVEL_WAITING_COSTS=" + data.travelWaitingCosts + ","
         + "WORK_CONCLUDED_DATE=" + data.workConcludedDate + ","
         + "NO_OF_SUSPECTS=" + data.noOfSuspects + ","
         + "NO_OF_POLICE_STATION=" + data.noOfPoliceStation + ","
         + "POLICE_STATION=" + data.policeStation + ","
         + "DUTY_SOLICITOR=" + data.dutySolicitor + ","
         + "YOUTH_COURT=" + data.youthCourt + ","
         + "SCHEME_ID=" + data.schemeId + ","
         + "DSCC_NUMBER=" + data.dsccNumber + ","
         + "POSTAL_APPL_ACCP=" + data.postalApplicationAccepted + ","
         + "NATIONAL_REF_MECHANISM_ADVICE=" + data.nrmAdvice + ","
         + "LEGACY_CASE=" + data.legacyCase + ","
         + "LONDON_NONLONDON_RATE=" + data.londonNonLondonRate + ","
         + "ADDITIONAL_TRAVEL_PAYMENT=" + data.additionalTravelPayment + ","
         + "ELIGIBLE_CLIENT_INDICATOR=" + data.eligibleClientIndicator + ","
         + "IRC_SURGERY=" + data.ircSurgery + ","
         + "SUBSTANTIVE_HEARING=" + data.substantiveHearing + ","
          + "TOLERANCE_INDICATOR=" + data.toleranceIndicator + ","
          + "REP_ORDER_DATE=" + data.repOrderDate + ","
          + "TRANSFER_DATE=" + data.transferDate + ","
          + "SURGERY_DATE=" + data.surgeryDate + ","
          + "CLIENT_LEGALLY_AIDED=" + data.clientLegallyAided;
   }

  // ==================== Utility Methods ====================

  private static String buildUfn(LocalDate caseStart, int caseNum) {
    return padNum(caseStart.getDayOfMonth(), 2)
        + padNum(caseStart.getMonthValue(), 2)
        + padNum(caseStart.getYear() % 100, 2)
        + "/"
        + padNum(caseNum + 1, 3);
  }

   private static String generateDscc() {
     int base = randomIntBetween(200000000, 299999999);
     String suffix = randomCharFrom("ABCD");
     return base + suffix;
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

  private static double randomAmount(double min, double max) {
    return min + (RANDOM.nextDouble() * (max - min));
  }

  private static int randomIntBetween(int min, int max) {
    return min + RANDOM.nextInt(max - min + 1);
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

  // ==================== Data Classes ====================

  private static class PoliceStation {
    String id;
    int schemeId;

    PoliceStation(String id, int schemeId) {
      this.id = id;
      this.schemeId = schemeId;
    }
  }

   private static class OutcomeData {
     String clientForename, clientSurname, clientDateOfBirth, gender, ethnicity, disability;
     String caseStartDate, profitCost, disbursementsAmount, disbursementsVat, vatIndicator;
     String travelCosts, outcomeCode, crimeMatterType, travelWaitingCosts, workConcludedDate;
     String noOfSuspects, noOfPoliceStation, policeStation, dutySolicitor, youthCourt;
     String schemeId, dsccNumber, postalApplicationAccepted, nrmAdvice, legacyCase;
     String londonNonLondonRate, additionalTravelPayment, eligibleClientIndicator;
     String ircSurgery, substantiveHearing, toleranceIndicator, repOrderDate, transferDate;
     String surgeryDate, clientLegallyAided, ufn;

    OutcomeData clientForename(String v) { this.clientForename = v; return this; }
    OutcomeData clientSurname(String v) { this.clientSurname = v; return this; }
    OutcomeData clientDateOfBirth(String v) { this.clientDateOfBirth = v; return this; }
    OutcomeData gender(String v) { this.gender = v; return this; }
    OutcomeData ethnicity(String v) { this.ethnicity = v; return this; }
    OutcomeData disability(String v) { this.disability = v; return this; }
    OutcomeData caseStartDate(String v) { this.caseStartDate = v; return this; }
    OutcomeData profitCost(String v) { this.profitCost = v; return this; }
    OutcomeData disbursementsAmount(String v) { this.disbursementsAmount = v; return this; }
    OutcomeData disbursementsVat(String v) { this.disbursementsVat = v; return this; }
    OutcomeData vatIndicator(String v) { this.vatIndicator = v; return this; }
    OutcomeData travelCosts(String v) { this.travelCosts = v; return this; }
    OutcomeData outcomeCode(String v) { this.outcomeCode = v; return this; }
    OutcomeData crimeMatterType(String v) { this.crimeMatterType = v; return this; }
    OutcomeData travelWaitingCosts(String v) { this.travelWaitingCosts = v; return this; }
    OutcomeData workConcludedDate(String v) { this.workConcludedDate = v; return this; }
    OutcomeData noOfSuspects(String v) { this.noOfSuspects = v; return this; }
    OutcomeData noOfPoliceStation(String v) { this.noOfPoliceStation = v; return this; }
    OutcomeData policeStation(String v) { this.policeStation = v; return this; }
    OutcomeData dutySolicitor(String v) { this.dutySolicitor = v; return this; }
    OutcomeData youthCourt(String v) { this.youthCourt = v; return this; }
    OutcomeData schemeId(String v) { this.schemeId = v; return this; }
    OutcomeData dsccNumber(String v) { this.dsccNumber = v; return this; }
    OutcomeData postalApplicationAccepted(String v) { this.postalApplicationAccepted = v; return this; }
    OutcomeData nrmAdvice(String v) { this.nrmAdvice = v; return this; }
    OutcomeData legacyCase(String v) { this.legacyCase = v; return this; }
    OutcomeData londonNonLondonRate(String v) { this.londonNonLondonRate = v; return this; }
    OutcomeData additionalTravelPayment(String v) { this.additionalTravelPayment = v; return this; }
    OutcomeData eligibleClientIndicator(String v) { this.eligibleClientIndicator = v; return this; }
    OutcomeData ircSurgery(String v) { this.ircSurgery = v; return this; }
    OutcomeData substantiveHearing(String v) { this.substantiveHearing = v; return this; }
    OutcomeData toleranceIndicator(String v) { this.toleranceIndicator = v; return this; }
    OutcomeData repOrderDate(String v) { this.repOrderDate = v; return this; }
    OutcomeData transferDate(String v) { this.transferDate = v; return this; }
    OutcomeData surgeryDate(String v) { this.surgeryDate = v; return this; }
     OutcomeData clientLegallyAided(String v) { this.clientLegallyAided = v; return this; }
     OutcomeData ufn(String v) { this.ufn = v; return this; }
   }
}

