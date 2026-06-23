package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Mediation files with field overrides for validation/negative/duplicate scenarios.
 * This wraps MediationGenerator and then mutates specific OUTCOME fields.
 * Direct Java equivalent to mediation override functionality in TS.
 */
public final class MediationGeneratorWithOverrides {

  private MediationGeneratorWithOverrides() {}

  /**
   * Generate Mediation files with field overrides applied to each outcome.
   */
  public static Path generateWithOverrides(
      String format,
      int outcomes,
      Path filePath,
      String office,
      List<Map<String, String>> overrides)
      throws IOException {
    
    // First, generate the base file
    List<Map<String, String>> emptyClaims = new ArrayList<>();
    for (int i = 0; i < outcomes; i++) {
      emptyClaims.add(new HashMap<>());
    }
    
    Path basePath = MediationGenerator.generateFromClaimsTable(format, emptyClaims, filePath);
    
    // Then apply field-level overrides to OUTCOME lines
    List<String> lines = Files.readAllLines(basePath, StandardCharsets.UTF_8);
    int outcomeIndex = 0;
    
    List<String> modified = new ArrayList<>();
    for (String line : lines) {
      if (!line.startsWith("OUTCOME")) {
        modified.add(line);
        continue;
      }
      
      Map<String, String> override = outcomeIndex < overrides.size() 
          ? overrides.get(outcomeIndex) 
          : new HashMap<>();
      outcomeIndex++;
      
      String modifiedLine = line;
      
      // Apply identifier overrides (UCN is client1's UCN only, not CLIENT2_UCN)
      if (override.containsKey("ucn")) {
        modifiedLine = modifiedLine.replaceAll("(?<!CLIENT2_)UCN=[^,]+", "UCN=" + override.get("ucn"));
      }
      if (override.containsKey("ufn")) {
        modifiedLine = modifiedLine.replaceAll("UFN=[^,]+", "UFN=" + override.get("ufn"));
      }
      if (override.containsKey("feeCode")) {
        modifiedLine = modifiedLine.replaceAll("FEE_CODE=[^,]+", "FEE_CODE=" + override.get("feeCode"));
      }
      
      // Apply date overrides
      if (override.containsKey("caseStartDate")) {
        modifiedLine = modifiedLine.replaceAll("CASE_START_DATE=[^,]+", 
            "CASE_START_DATE=" + override.get("caseStartDate"));
      }
      if (override.containsKey("medConcludedDate")) {
        modifiedLine = modifiedLine.replaceAll("MED_CONCLUDED_DATE=[^,]+",
            "MED_CONCLUDED_DATE=" + override.get("medConcludedDate"));
      }
      if (override.containsKey("workConcludedDate")) {
        modifiedLine = modifiedLine.replaceAll("WORK_CONCLUDED_DATE=[^,]+",
            "WORK_CONCLUDED_DATE=" + override.get("workConcludedDate"));
      }
      
      // Apply amount overrides
      if (override.containsKey("disbursementAmount")) {
        modifiedLine = modifiedLine.replaceAll("DISBURSEMENTS_AMOUNT=[^,]+",
            "DISBURSEMENTS_AMOUNT=" + override.get("disbursementAmount"));
      }
      if (override.containsKey("disbursementVat")) {
        modifiedLine = modifiedLine.replaceAll("DISBURSEMENTS_VAT=[^,]+",
            "DISBURSEMENTS_VAT=" + override.get("disbursementVat"));
      }
      
      // Apply sessions/time overrides
      if (override.containsKey("sessions")) {
        modifiedLine = modifiedLine.replaceAll("NUMBER_OF_MEDIATION_SESSIONS=[^,]+",
            "NUMBER_OF_MEDIATION_SESSIONS=" + override.get("sessions"));
      }
      if (override.containsKey("mediationTime")) {
        modifiedLine = modifiedLine.replaceAll("MEDIATION_TIME=[^,]+",
            "MEDIATION_TIME=" + override.get("mediationTime"));
      }
      
      // Apply Y/N indicator overrides
      if (override.containsKey("vatIndicator")) {
        modifiedLine = modifiedLine.replaceAll("VAT_INDICATOR=[^,]+",
            "VAT_INDICATOR=" + override.get("vatIndicator"));
      }
      
      modified.add(modifiedLine);
    }
    
    Files.write(basePath, modified, StandardCharsets.UTF_8);
    return basePath;
  }
}

