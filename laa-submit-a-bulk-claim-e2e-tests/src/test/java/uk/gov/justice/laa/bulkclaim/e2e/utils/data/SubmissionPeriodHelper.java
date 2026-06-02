package uk.gov.justice.laa.bulkclaim.e2e.utils.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.bulkclaim.e2e.utils.db.DatabaseManager;

/**
 * Java parity helper for TS submissionPeriodHelper.ts.
 * Picks unique, contract-valid submission periods using DB + provider/fsp APIs.
 */
@Slf4j
public final class SubmissionPeriodHelper {

  private static final List<String> LEGAL_HELP_OFFICES =
      List.of("0P322F", "2L847Q", "2N199K", "2P746R", "1T102C");

  private static final List<String> MONTHS =
      List.of("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC");

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  // ⭐ THREAD-LOCAL: Each test thread gets its own submission period cache
  private static final ThreadLocal<Map<String, Set<String>>> USED_PERIODS_TL =
      ThreadLocal.withInitial(ConcurrentHashMap::new);

  private static final Set<String> USED_SCHEDULE_REFS = Collections.synchronizedSet(new HashSet<>());
  private static final Map<String, ContractValidity> PROVIDER_CACHE = new ConcurrentHashMap<>();

  private static final DatabaseManager SUBMISSION_MANAGER = new DatabaseManager("submissionPeriodHelper");

  private SubmissionPeriodHelper() {}

  /**
   * Clear thread-local context for current thread.
   * Call in @Before hook to isolate each scenario's submission period cache.
   */
  public static void clearThreadContext() {
    USED_PERIODS_TL.remove();
  }

  /**
   * Get the thread-local USED_PERIODS cache for the current execution thread.
   */
  private static Map<String, Set<String>> getUsedPeriods() {
    return USED_PERIODS_TL.get();
  }

  public static List<String> allowedPeriods() {
    int startYear = 2015;
    LocalDate now = LocalDate.now();
    YearMonth lastFullMonth = YearMonth.from(now).minusMonths(1);

    List<String> periods = new ArrayList<>();
    for (int year = startYear; year <= lastFullMonth.getYear(); year++) {
      int monthEnd = (year == lastFullMonth.getYear()) ? lastFullMonth.getMonthValue() - 1 : 11;
      for (int month = 0; month <= monthEnd; month++) {
        periods.add(MONTHS.get(month) + "-" + year);
      }
    }

    log.info("Generated {} allowed submission periods", periods.size());
    log.debug("Allowed submission periods: {}", periods);
    return periods;
  }

  public static SubmissionPeriodResult getUniqueSubmissionPeriod(String account, String areaOfLaw, String feeCode) {
    List<String> allowed = allowedPeriods();
    if (allowed.isEmpty()) {
      throw new IllegalStateException("No allowed submission periods available");
    }

    String accountKey = account.trim();
    String providerLawKey = normaliseProviderAreaOfLaw(areaOfLaw);
    String dbLawKey = normaliseDbAreaOfLaw(areaOfLaw);

    String categoryOfLawCode = null;
    if (feeCode != null && !feeCode.isBlank()) {
      FeeDetails details = fetchFeeDetails(feeCode);
      categoryOfLawCode = details.categoryOfLawCode();
    }

    String catKey = categoryOfLawCode != null ? categoryOfLawCode : "ANY";
    String cacheKey = dbLawKey + "_" + accountKey + "_" + catKey;

    Map<String, Set<String>> usedPeriods = getUsedPeriods();
    usedPeriods.putIfAbsent(cacheKey, Collections.synchronizedSet(new HashSet<>()));

    boolean hasDb = SUBMISSION_MANAGER.ensureInitialized();
    List<String> candidates = new ArrayList<>(allowed);
    Collections.reverse(candidates);

    synchronized (usedPeriods.get(cacheKey)) {
      for (String candidate : candidates) {
        if (usedPeriods.get(cacheKey).contains(candidate)) {
          continue;
        }

        if (hasDb && existsSubmissionInDb(dbLawKey, candidate, accountKey)) {
          continue;
        }

        ContractValidity contract = hasValidContract(accountKey, providerLawKey, candidate, categoryOfLawCode);
        if (!contract.valid()) {
          continue;
        }

        usedPeriods.get(cacheKey).add(candidate);
        return new SubmissionPeriodResult(candidate, contract.start(), contract.end());
      }
    }

    throw new IllegalStateException("No available submission period for " + cacheKey);
  }

  public static String generateScheduleRef(String account) {
    String sanitized = account.trim();
    int year = LocalDate.now().getYear();
    String candidate;
    do {
      int suffix = (int) (Math.random() * 1000);
      candidate = sanitized + "/" + year + "/" + String.format(Locale.ROOT, "%03d", suffix);
    } while (USED_SCHEDULE_REFS.contains(candidate));
    USED_SCHEDULE_REFS.add(candidate);
    return candidate;
  }

  public static String getSubmissionPeriod(String monthIncrement, boolean shortFormat) {
    int increment = Integer.parseInt(monthIncrement.replace("+", ""));
    LocalDate date = LocalDate.now().plusMonths(increment);
    if (shortFormat) {
      return MONTHS.get(date.getMonthValue() - 1) + "-" + date.getYear();
    }
    return date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK));
  }

  public static void lockSpecificPeriod(String account, String areaOfLaw, String period) {
    String dbLawKey = normaliseDbAreaOfLaw(areaOfLaw);
    String accountKey = account.trim();
    String cacheKey = dbLawKey + "_" + accountKey + "_ANY";

    Map<String, Set<String>> usedPeriods = getUsedPeriods();
    usedPeriods.putIfAbsent(cacheKey, Collections.synchronizedSet(new HashSet<>()));

    synchronized (usedPeriods.get(cacheKey)) {
      if (SUBMISSION_MANAGER.ensureInitialized() && existsSubmissionInDb(dbLawKey, period, accountKey)) {
        throw new IllegalStateException("Cannot lock period " + period + " - already used in DB");
      }
      usedPeriods.get(cacheKey).add(period);
    }
  }

  public static void resetSubmissionPeriodCache() {
    getUsedPeriods().clear();
    USED_SCHEDULE_REFS.clear();
    PROVIDER_CACHE.clear();
  }

  public static void destroySubmissionPeriodManager() {
    SUBMISSION_MANAGER.destroy();
  }

  public static PeriodRangeResult findTwoValidPeriodsApart(
      String requestedOffice, String areaOfLaw, int monthsDifference) {
    List<String> candidates = new ArrayList<>(allowedPeriods());
    Collections.reverse(candidates);

    List<String> officesToTry = new ArrayList<>();
    officesToTry.add(requestedOffice);
    for (String office : LEGAL_HELP_OFFICES) {
      if (!office.equals(requestedOffice)) {
        officesToTry.add(office);
      }
    }

    for (String office : officesToTry) {
      for (String period1 : candidates) {
        log.info("Checking period {} for office {} and area {}", period1, office, areaOfLaw);
        if (!isPeriodAvailable(office, areaOfLaw, period1)) {
          continue;
        }

        String period2 = shiftPeriod(period1, monthsDifference);
        if (!allowedPeriods().contains(period2) || !isPeriodAvailable(office, areaOfLaw, period2)) {
          continue;
        }

        lockSpecificPeriod(office, areaOfLaw, period1);
        lockSpecificPeriod(office, areaOfLaw, period2);
        return new PeriodRangeResult(office, period1, period2);
      }
    }

    throw new IllegalStateException(
        "Could not find two valid submission periods "
            + monthsDifference
            + " months apart for any office in area "
            + areaOfLaw);
  }

  public static String shiftPeriod(String period, int monthDifference) {
    int month = MONTHS.indexOf(period.substring(0, 3).toUpperCase(Locale.ROOT));
    int year = Integer.parseInt(period.substring(4));
    LocalDate shifted = LocalDate.of(year, month + 1, 1).plusMonths(monthDifference);
    return MONTHS.get(shifted.getMonthValue() - 1) + "-" + shifted.getYear();
  }

  private static boolean existsSubmissionInDb(String dbLawKey, String period, String account) {
    try {
      String sql =
          "SELECT 1 FROM claims.submission WHERE area_of_law = '"
              + dbLawKey
              + "' AND submission_period = '"
              + period
              + "' AND office_account_number = '"
              + account
              + "' LIMIT 1";
      return !SUBMISSION_MANAGER.query(sql).isEmpty();
    } catch (Exception ignored) {
      return false;
    }
  }

  private static boolean isPeriodAvailable(String office, String areaOfLaw, String period) {
    String dbLawKey = normaliseDbAreaOfLaw(areaOfLaw);
    String providerLawKey = normaliseProviderAreaOfLaw(areaOfLaw);

    if (SUBMISSION_MANAGER.ensureInitialized() && existsSubmissionInDb(dbLawKey, period, office)) {
      return false;
    }

    String cacheKey = dbLawKey + "_" + office.trim() + "_ANY";
    Map<String, Set<String>> usedPeriods = getUsedPeriods();
    if (usedPeriods.containsKey(cacheKey) && usedPeriods.get(cacheKey).contains(period)) {
      return false;
    }

    return hasValidContract(office.trim(), providerLawKey, period, null).valid();
  }

  private static FeeDetails fetchFeeDetails(String feeCode) {
    String base = envOrDefault("FSP_API_BASE_URL", "");
    if (base.isBlank()) {
      return new FeeDetails(null, null);
    }

    try {
      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(base + "/api/v1/fee-details/" + feeCode))
              .GET()
              .header("Content-Type", "application/json")
              .header("Accept", "application/json");

      String token = System.getenv("FSP_API_TOKEN");
      if (token != null && !token.isBlank()) {
        builder.header("Authorization", token);
      }

      HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        return new FeeDetails(null, null);
      }

      JsonNode node = MAPPER.readTree(response.body());
      String category = textOrNull(node.get("categoryOfLawCode"));
      String feeType = textOrNull(node.get("feeType"));
      return new FeeDetails(category, feeType);
    } catch (Exception e) {
      return new FeeDetails(null, null);
    }
  }

  private static ContractValidity hasValidContract(
      String office,
      String providerAreaOfLaw,
      String period,
      String categoryOfLawCode) {
    String cacheKey = office + "_" + providerAreaOfLaw + "_" + period + "_" + (categoryOfLawCode == null ? "ANY" : categoryOfLawCode);
    if (PROVIDER_CACHE.containsKey(cacheKey)) {
      return PROVIDER_CACHE.get(cacheKey);
    }

    int month = MONTHS.indexOf(period.substring(0, 3));
    int year = Integer.parseInt(period.substring(4));
    String effectiveDate = LocalDate.of(year, month + 1, 15).toString();

    String providerBase = envOrDefault(
        "PROVIDER_API",
        "https://laa-provider-details-api-uat.apps.live.cloud-platform.service.justice.gov.uk/api/v1/provider-offices");

    try {
      String url = providerBase + "/" + office + "/schedules?effectiveDate=" + effectiveDate + "&areaOfLaw=" + providerAreaOfLaw.replace(" ", "%20");
      HttpRequest.Builder builder =
          HttpRequest.newBuilder().uri(URI.create(url)).GET().header("accept", "application/json");

      String providerApiKey = System.getenv("PROVIDER_API_KEY");
      if (providerApiKey != null && !providerApiKey.isBlank()) {
        builder.header("X-Authorization", providerApiKey);
      }

      HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        ContractValidity invalid = new ContractValidity(false, null, null);
        PROVIDER_CACHE.put(cacheKey, invalid);
        return invalid;
      }

      JsonNode root = MAPPER.readTree(response.body());
      JsonNode schedules = root.path("schedules");
      if (!schedules.isArray() || schedules.isEmpty()) {
        ContractValidity invalid = new ContractValidity(false, null, null);
        PROVIDER_CACHE.put(cacheKey, invalid);
        return invalid;
      }

      LocalDate effective = LocalDate.parse(effectiveDate);
      for (JsonNode s : schedules) {
        LocalDate start = LocalDate.parse(textOrNull(s.get("scheduleStartDate")));
        LocalDate end = LocalDate.parse(textOrNull(s.get("scheduleEndDate")));
        boolean dateMatch = !effective.isBefore(start) && !effective.isAfter(end);
        if (!dateMatch) {
          continue;
        }

        if (categoryOfLawCode != null && s.has("scheduleLines") && s.get("scheduleLines").isArray()) {
          boolean categoryMatch = false;
          for (JsonNode line : s.get("scheduleLines")) {
            if (categoryOfLawCode.equals(textOrNull(line.get("categoryOfLaw")))) {
              categoryMatch = true;
              break;
            }
          }
          if (!categoryMatch) {
            continue;
          }
        }

        ContractValidity valid = new ContractValidity(true, start.toString(), end.toString());
        PROVIDER_CACHE.put(cacheKey, valid);
        return valid;
      }

      ContractValidity invalid = new ContractValidity(false, null, null);
      PROVIDER_CACHE.put(cacheKey, invalid);
      return invalid;
    } catch (Exception e) {
      ContractValidity invalid = new ContractValidity(false, null, null);
      PROVIDER_CACHE.put(cacheKey, invalid);
      return invalid;
    }
  }

  private static String normaliseProviderAreaOfLaw(String area) {
    return area.trim().toUpperCase(Locale.ROOT);
  }

  private static String normaliseDbAreaOfLaw(String area) {
    String key = area.trim().toUpperCase(Locale.ROOT);
    Map<String, String> map = new HashMap<>();
    map.put("LEGAL HELP", "LEGAL_HELP");
    map.put("CRIME LOWER", "CRIME_LOWER");
    map.put("MEDIATION", "MEDIATION");
    return map.getOrDefault(key, key.replace(" ", "_"));
  }

  private static String textOrNull(JsonNode node) {
    return node == null || node.isNull() ? null : node.asText();
  }

  private static String envOrDefault(String name, String defaultValue) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? defaultValue : value;
  }

  public record SubmissionPeriodResult(String period, String scheduleStart, String scheduleEnd) {}

  public record PeriodRangeResult(String officeUsed, String period1, String period2) {}

  private record FeeDetails(String categoryOfLawCode, String feeType) {}

  private record ContractValidity(boolean valid, String start, String end) {}
}

