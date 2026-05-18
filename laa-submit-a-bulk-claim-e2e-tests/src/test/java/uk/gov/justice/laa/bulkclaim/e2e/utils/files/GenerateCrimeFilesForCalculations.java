package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Crime files optimized for fee calculation testing.
 * Produces claims with specific combinations of amounts, times, and matter types.
 * Direct Java equivalent to generateCrimeFilesForCalculations.ts
 */
public final class GenerateCrimeFilesForCalculations {

  private GenerateCrimeFilesForCalculations() {}

  /** TS-like signature parity: GenerateCrimeFilesForCalculations(files, outcomes, format, options). */
  public static GeneratorResult GenerateCrimeFilesForCalculations(
      int files,
      int outcomes,
      String format,
      GenerateFileOptions options)
      throws IOException {
    GenerateFileOptions opts = options == null ? new GenerateFileOptions() : options;
    GeneratorResult result = CrimeGenerator.GenerateCrimeFiles(files, outcomes, format, opts);
    return result;
  }

  /**
   * Generate Crime file with data optimized for fee calculation verification.
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
      claim.put("profitCost", String.format("%.2f", (i + 1) * 50.00)); // Varies by index
      claim.put("disbursementAmount", String.format("%.2f", (i + 1) * 10.00));
      claim.put("travelWaitingCosts", String.format("%.2f", (i + 1) * 5.00));
      claims.add(claim);
    }
    
    return CrimeGenerator.generateFromClaimsTable(format, claims, filePath);
  }

  /**
   * Generate with custom calculation test data.
   */
  public static Path generateFileWithClaims(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    
    return CrimeGenerator.generateFromClaimsTable(format, claimRows, filePath);
  }
}

