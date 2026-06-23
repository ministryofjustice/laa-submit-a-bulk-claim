package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Generates Crime files with fixed (non-randomized) data for specific test scenarios.
 * Direct Java equivalent to generateFixedCrimeFiles.ts
 */
public final class GenerateFixedCrimeFiles {

  private GenerateFixedCrimeFiles() {}

  /**
   * Generate Crime file with fixed/predetermined data.
   * Used for deterministic testing scenarios.
   */
  public static Path generateFile(
      String format,
      int outcomes,
      Path filePath,
      String office)
      throws IOException {
    
    // Generate base file
    List<Map<String, String>> emptyClaims = new java.util.ArrayList<>();
    for (int i = 0; i < outcomes; i++) {
      emptyClaims.add(new java.util.HashMap<>());
    }
    
    return CrimeGenerator.generateFromClaimsTable(format, emptyClaims, filePath);
  }

  /**
   * Generate with specific overrides for deterministic test data.
   */
  public static Path generateFileWithOverrides(
      String format,
      int outcomes,
      Path filePath,
      String office,
      List<Map<String, String>> overrides)
      throws IOException {
    
    return CrimeGeneratorWithOverrides.generateWithOverrides(
        format, outcomes, filePath, office, overrides);
  }
}

