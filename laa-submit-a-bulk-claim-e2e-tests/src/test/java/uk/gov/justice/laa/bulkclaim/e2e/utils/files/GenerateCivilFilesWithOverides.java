package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Civil (Legal Help) files with overrides applied via path-based configuration.
 * Direct Java equivalent to generateCivilFilesWithOverides.ts with path support.
 */
public final class GenerateCivilFilesWithOverides {

  private GenerateCivilFilesWithOverides() {}

  /**
   * Generate Civil files with field-level overrides.
   */
  public static Path generateFileWithOverrides(
      String format,
      int outcomes,
      Path filePath,
      String office,
      List<Map<String, String>> overrides)
      throws IOException {
    
    return LegalHelpGeneratorWithOverrides.generateWithOverrides(
        format, outcomes, filePath, office, overrides);
  }

  /**
   * Generate base Civil file then apply overrides.
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
    
    return LegalHelpGenerator.generateFromClaimsTable(format, emptyClaims, filePath);
  }
}

