package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Legal Help files optimized for fee calculation testing.
 * Produces claims with specific combinations of amounts, times, and matter types.
 * Direct Java equivalent to calculateCivilFiles or similar.
 */
public final class GenerateLegalHelpFilesForCalculations {

  private GenerateLegalHelpFilesForCalculations() {}

  /**
   * Generate Legal Help file with data optimized for fee calculation verification.
   */
  public static Path generateFile(
      String format,
      int outcomes,
      Path filePath,
      String office)
      throws IOException {
    
    List<Map<String, String>> claims = new java.util.ArrayList<>();
    
    // Generate claims with varied amounts for calculation testing
    for (int i = 0; i < outcomes; i++) {
      Map<String, String> claim = new HashMap<>();
      claim.put("profitCost", String.format("%.2f", (i + 1) * 60.00)); // Varies by index
      claim.put("disbursementAmount", String.format("%.2f", (i + 1) * 15.00));
      claim.put("adviceTime", String.valueOf((i + 1) * 60)); // Varies time
      claims.add(claim);
    }
    
    return LegalHelpGenerator.generateFromClaimsTable(format, claims, filePath);
  }

  /**
   * Generate with custom calculation test data.
   */
  public static Path generateFileWithClaims(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    
    return LegalHelpGenerator.generateFromClaimsTable(format, claimRows, filePath);
  }
}

