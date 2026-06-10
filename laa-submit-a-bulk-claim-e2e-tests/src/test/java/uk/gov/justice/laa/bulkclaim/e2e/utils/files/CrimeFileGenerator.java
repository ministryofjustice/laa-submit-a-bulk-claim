package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Domain generator facade for crime payloads. */
public final class CrimeFileGenerator {

  private CrimeFileGenerator() {}

  public static Path generateMinimalSubmissionFile(String format, int outcomes, Path filePath)
      throws IOException {
    return FileGeneratorUtil.generateMinimalSubmissionFile("Crime lower", format, outcomes, filePath);
  }

  public static Path generateFromClaimsTable(
      String format,
      List<Map<String, String>> claimRows,
      Path filePath)
      throws IOException {
    return FileGeneratorUtil.generateFromClaimsTable("Crime lower", format, claimRows, filePath);
  }
}

