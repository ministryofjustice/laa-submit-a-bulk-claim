package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Legal Help files with immigration-specific bolt-ons and overrides.
 * Direct Java equivalent to GenerateCivilImmigrationBoltOnsOverride.ts
 */
public final class GenerateCivilImmigrationBoltOnsOverride {

  private GenerateCivilImmigrationBoltOnsOverride() {}

  /**
   * Generate Civil (Immigration) file with bolt-on specific fields and overrides.
   * Immigration claims have additional fields like Prior Authority Reference.
   */
  public static Path generateFileWithBoltOns(
      String format,
      int outcomes,
      Path filePath,
      String office,
      List<Map<String, String>> overrides)
      throws IOException {
    
    List<Map<String, String>> enhancedOverrides = new java.util.ArrayList<>();
    
    for (Map<String, String> override : overrides) {
      Map<String, String> enhanced = new HashMap<>(override);
      
      // Add immigration-specific fields if not already present
      enhanced.putIfAbsent("priorAuthorityRef", "1234567");
      // Add other immigration bolt-ons as needed
      
      enhancedOverrides.add(enhanced);
    }
    
    return LegalHelpGeneratorWithOverrides.generateWithOverrides(
        format, outcomes, filePath, office, enhancedOverrides);
  }
}

