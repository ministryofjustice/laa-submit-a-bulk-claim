package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Legal Help Immigration files optimized for fee calculation testing.
 * Produces immigration-specific claims with varied amounts for calculation verification.
 * Direct Java equivalent to GenerateLegalHelpImmigrationFilesForCalculations.ts
 */
public final class GenerateLegalHelpImmigrationFilesForCalculations {

  private GenerateLegalHelpImmigrationFilesForCalculations() {}

  /**
   * Generate Immigration Legal Help file with calculation test data.
   */
  public static Path generateFile(
      String format,
      int outcomes,
      Path filePath)
      throws IOException {

    List<Map<String, String>> claims = new java.util.ArrayList<>();

    // Generate claims with immigration-specific fields and varied amounts
    for (int i = 0; i < outcomes; i++) {
      claims.add(buildImmigrationClaim(i));
    }

    return LegalHelpGenerator.generateFromClaimsTable(format, claims, filePath);
  }

  /**
   * Generate with custom immigration test data.
   */
  public static Path generateFileWithClaims(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {

    return LegalHelpGenerator.generateFromClaimsTable(format, claimRows, filePath);
  }

  /**
   * Build immigration-specific claim data for calculation testing.
   */
  private static Map<String, String> buildImmigrationClaim(int index) {
    Map<String, String> claim = new HashMap<>();
    // Use correct field names that match the Legal Help schema
    claim.put("profitCost", String.format("%.2f", (index + 1) * 75.00));
    claim.put("disbursementsAmount", String.format("%.2f", (index + 1) * 20.00));
    claim.put("disbursementsVat", String.format("%.2f", (index + 1) * 1.98));
    claim.put("outcomeCode", "FX");
    return claim;
  }
}

