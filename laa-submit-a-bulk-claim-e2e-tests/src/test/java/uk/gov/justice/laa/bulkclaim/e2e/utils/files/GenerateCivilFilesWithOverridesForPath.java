package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Generates Legal Help files with field overrides using path-based configuration.
 * Direct Java equivalent to generateCivilFilesWithOverridesForPath.ts
 */
public final class GenerateCivilFilesWithOverridesForPath {

  private GenerateCivilFilesWithOverridesForPath() {}

  /**
   * Generate Civil files from a path with overrides applied.
   * Reads overrides from path-based configuration.
   */
  public static Path generateFileWithOverrides(
      String format,
      int outcomes,
      Path filePath,
      String office,
      Path overridePath,
      List<Map<String, String>> overrides)
      throws IOException {
    
    // Generate base file and apply overrides
    return LegalHelpGeneratorWithOverrides.generateWithOverrides(
        format, outcomes, filePath, office, overrides);
  }

  /**
   * Generate with path-based overrides (for extensible configuration).
   */
  public static Path generateFile(
      String format,
      int outcomes,
      Path filePath,
      String office,
      List<Map<String, String>> overrides)
      throws IOException {
    
    return LegalHelpGeneratorWithOverrides.generateWithOverrides(
        format, outcomes, filePath, office, overrides);
  }
}

