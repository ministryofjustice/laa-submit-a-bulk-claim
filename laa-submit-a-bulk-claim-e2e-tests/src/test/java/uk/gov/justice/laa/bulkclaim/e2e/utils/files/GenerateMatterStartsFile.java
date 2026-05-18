package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;

/** Direct Java equivalent to generateMatterStartsFile.ts. */
public final class GenerateMatterStartsFile {

  private static final DateTimeFormatter SHORT_PERIOD = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);

  private static final List<String> LEGAL_HELP_CODES =
      List.of("AAP", "COM", "CON", "DEB", "EDU", "EMP", "ELA", "HOU", "IMMAS",
          "IMMOT", "MAT", "MED", "MHE", "MSC", "PI", "PUB", "WB", "DISC");

  private static final List<String> MEDIATION_CODES =
      List.of("MDCS", "MDCC", "MDPS", "MDPC", "MDAS", "MDAC");

  private GenerateMatterStartsFile() {}

  public record MatterStartsGenerationResult(
      Path filePath,
      String fileName,
      Map<String, Integer> counts,
      String submissionPeriod,
      String scheduleRef,
      String officeAccount) {}

  public static MatterStartsGenerationResult generateFile(
      String areaOfLaw,
      String format,
      Path filePath,
      boolean includeAllCodes)
      throws IOException {
    if (!"csv".equalsIgnoreCase(format)) {
      throw new IOException("Matter starts generation only supports csv format (got " + format + ")");
    }

    String normalizedArea = areaOfLaw == null ? "" : areaOfLaw.trim().toLowerCase(Locale.ROOT);
    AreaConfig config = areaConfig(normalizedArea);
    SubmissionPeriodHelper.SubmissionPeriodResult period;
    try {
      period = SubmissionPeriodHelper.getUniqueSubmissionPeriod(config.account(), config.dbAreaOfLaw(), null);
    } catch (Exception e) {
      String fallback = java.time.YearMonth.now().minusMonths(1).format(SHORT_PERIOD).toUpperCase(Locale.ROOT);
      period = new SubmissionPeriodHelper.SubmissionPeriodResult(fallback, null, null);
    }

    String scheduleRef = SubmissionPeriodHelper.generateScheduleRef(config.account());
    List<String> codes = config.codes();
    Map<String, Integer> counts = new LinkedHashMap<>();
    if (includeAllCodes) {
      for (int i = 0; i < codes.size(); i++) {
        counts.put(codes.get(i), i + 1);
      }
    } else if (!codes.isEmpty()) {
      counts.put(codes.get(0), 1);
    }

    String scheduleNum = switch (normalizedArea) {
      case "mediation" -> {
        String mon = period.period().substring(0, 3).toUpperCase(Locale.ROOT);
        String yr = period.period().substring(period.period().length() - 2);
        yield config.account() + "/MEDI" + mon + yr + "/01";
      }
      default -> config.account() + "/CIVIL";
    };

    StringBuilder content = new StringBuilder();
    content.append("OFFICE,account=").append(config.account()).append("\n");
    content.append("SCHEDULE,submissionPeriod=")
        .append(period.period())
        .append(",areaOfLaw=")
        .append(config.dbAreaOfLaw())
        .append(",scheduleNum=")
        .append(scheduleNum)
        .append("\n");
    content.append("MATTERSTARTS,SCHEDULE_REF=")
        .append(scheduleRef)
        .append(",PROCUREMENT_AREA=")
        .append(config.procurementArea())
        .append(",ACCESS_POINT=AP00000");
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      content.append(",").append(entry.getKey()).append("=").append(entry.getValue());
    }
    content.append("\n");

    Path parent = filePath.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }
    Files.writeString(filePath, content.toString(), StandardCharsets.UTF_8);

    return new MatterStartsGenerationResult(
        filePath,
        filePath.getFileName().toString(),
        counts,
        period.period(),
        scheduleRef,
        config.account());
  }

  private static AreaConfig areaConfig(String areaKey) {
    return switch (areaKey) {
      case "mediation" -> new AreaConfig("0P322F", "MEDIATION", "PA00120", MEDIATION_CODES);
      case "legal help" -> new AreaConfig("0P322F", "LEGAL HELP", "PA00186", LEGAL_HELP_CODES);
      default -> throw new IllegalArgumentException(
          "Matter starts generation is not supported for area of law: " + areaKey);
    };
  }

  private record AreaConfig(String account, String dbAreaOfLaw, String procurementArea, List<String> codes) {}
}

