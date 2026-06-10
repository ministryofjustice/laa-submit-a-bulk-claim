package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Generates a single Legal Help submission file.
 * Wrapper around LegalHelpGenerator for simplified single-file generation.
 * Direct Java equivalent to generateSingleLegalHelpFile.ts
 */
public final class GenerateSingleLegalHelpFile {

  private GenerateSingleLegalHelpFile() {}

  /**
   * Generate a single Legal Help file with specified outcomes.
   */
  public static Path generateFile(
      String format,
      int outcomes,
      Path filePath)
      throws IOException {
    List<Map<String, String>> emptyClaims = new java.util.ArrayList<>();
    for (int i = 0; i < outcomes; i++) {
      emptyClaims.add(new java.util.HashMap<>());
    }
    return LegalHelpGenerator.generateFromClaimsTable(format, emptyClaims, filePath);
  }

  /**
   * Generate single file with custom claims/overrides.
   */
  public static Path generateFileWithClaims(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    return LegalHelpGenerator.generateFromClaimsTable(format, claimRows, filePath);
  }
}

