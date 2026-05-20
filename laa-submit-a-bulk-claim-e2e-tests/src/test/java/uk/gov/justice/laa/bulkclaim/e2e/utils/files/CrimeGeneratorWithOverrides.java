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
 * Generates Crime files with field overrides for validation/negative/duplicate scenarios.
 * This wraps CrimeGenerator and then mutates specific OUTCOME fields.
 * Direct Java equivalent to crime override functionality in TS.
 */
public final class CrimeGeneratorWithOverrides {

  private CrimeGeneratorWithOverrides() {}

  /**
   * Generate Crime files with field overrides applied to each outcome.
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
    
    Path basePath = CrimeGenerator.generateFromClaimsTable(format, emptyClaims, filePath);
    
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
      
      // Apply identifier overrides
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
      if (override.containsKey("workConcludedDate")) {
        modifiedLine = modifiedLine.replaceAll("WORK_CONCLUDED_DATE=[^,]+",
            "WORK_CONCLUDED_DATE=" + override.get("workConcludedDate"));
      }
      if (override.containsKey("transferDate")) {
        modifiedLine = modifiedLine.replaceAll("TRANSFER_DATE=[^,]+",
            "TRANSFER_DATE=" + override.get("transferDate"));
      }
      if (override.containsKey("repOrderDate")) {
        modifiedLine = modifiedLine.replaceAll("REP_ORDER_DATE=[^,]+",
            "REP_ORDER_DATE=" + override.get("repOrderDate"));
      }
      if (override.containsKey("surgeryDate")) {
        modifiedLine = modifiedLine.replaceAll("SURGERY_DATE=[^,]+",
            "SURGERY_DATE=" + override.get("surgeryDate"));
      }
      
      // Apply amount overrides
      if (override.containsKey("profitCost")) {
        modifiedLine = modifiedLine.replaceAll("PROFIT_COST=[^,]+",
            "PROFIT_COST=" + override.get("profitCost"));
      }
      if (override.containsKey("disbursementAmount")) {
        modifiedLine = modifiedLine.replaceAll("DISBURSEMENTS_AMOUNT=[^,]+",
            "DISBURSEMENTS_AMOUNT=" + override.get("disbursementAmount"));
      }
      if (override.containsKey("disbursementVat")) {
        modifiedLine = modifiedLine.replaceAll("DISBURSEMENTS_VAT=[^,]+",
            "DISBURSEMENTS_VAT=" + override.get("disbursementVat"));
      }
      
      // Apply Y/N indicator overrides
      if (override.containsKey("vatIndicator")) {
        modifiedLine = modifiedLine.replaceAll("VAT_INDICATOR=[^,]+",
            "VAT_INDICATOR=" + override.get("vatIndicator"));
      }
      if (override.containsKey("dutySolicitor")) {
        modifiedLine = modifiedLine.replaceAll("DUTY_SOLICITOR=[^,]+",
            "DUTY_SOLICITOR=" + override.get("dutySolicitor"));
      }
      if (override.containsKey("youthCourt")) {
        modifiedLine = modifiedLine.replaceAll("YOUTH_COURT=[^,]+",
            "YOUTH_COURT=" + override.get("youthCourt"));
      }
      
      modified.add(modifiedLine);
    }
    
    Files.write(basePath, modified, StandardCharsets.UTF_8);
    return basePath;
  }
}

