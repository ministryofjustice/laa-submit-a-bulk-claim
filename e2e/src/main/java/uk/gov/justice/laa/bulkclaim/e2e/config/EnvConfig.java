package uk.gov.justice.laa.bulkclaim.e2e.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvConfig {
  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

  private static String getOrDefault(String key, String defaultValue) {
    return dotenv.get(key, defaultValue);
  }

  public static String baseUrl() {
    return getOrDefault("E2E_BASE_URL", "http://localhost:8082/");
  }

  public static boolean headless() {
    return Boolean.parseBoolean(getOrDefault("E2E_HEADLESS", "true"));
  }

  public static String authMethod() {
    return getOrDefault("E2E_AUTH_METHOD", "silas");
  }

  public static String silasUsername() {
    return getOrDefault("E2E_SILAS_USERNAME", "standard_user");
  }

  public static String silasPassword() {
    return getOrDefault("E2E_SILAS_PASSWORD", "secret_sauce");
  }

  public static String silasMfaSecret() {
    return getOrDefault("E2E_SILAS_MFA_SECRET", "");
  }

  public static String dbConnectionUrl() {
    return getOrDefault("E2E_DB_CONNECTION_URL", "jdbc:postgresql://localhost:5432/dbname");
  }

  public static String dbUser() {
    return getOrDefault("E2E_DB_USER", "postgres");
  }

  public static String dbPassword() {
    return getOrDefault("E2E_DB_PASSWORD", "");
  }

  public static String userId() {
    return "LAA-Submit-A-Bulk-Claim-E2E-Tests";
  }
}
