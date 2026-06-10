package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Mediation files with overrides applied via path-based configuration.
 * Direct Java equivalent to genarateMediationFilesWithOverides.ts
 */
public final class GenerateMediationFilesWithOverrides {

  private GenerateMediationFilesWithOverrides() {}

  /**
   * Generate Mediation files with field-level overrides.
   */
  public static Path generateFileWithOverrides(
      String format,
      int outcomes,
      Path filePath,
      String office,
      List<Map<String, String>> overrides)
      throws IOException {
    
    return MediationGeneratorWithOverrides.generateWithOverrides(
        format, outcomes, filePath, office, overrides);
  }

  /**
   * Generate base Mediation file then apply overrides.
   */
  public static Path generateFile(
      String format,
      int outcomes,
      Path filePath,
      String office)
      throws IOException {
    
    List<Map<String, String>> emptyClaims = new java.util.ArrayList<>();
    for (int i = 0; i < outcomes; i++) {
      emptyClaims.add(new HashMap<>());
    }
    
    return MediationGenerator.generateFromClaimsTable(format, emptyClaims, filePath);
  }
}

