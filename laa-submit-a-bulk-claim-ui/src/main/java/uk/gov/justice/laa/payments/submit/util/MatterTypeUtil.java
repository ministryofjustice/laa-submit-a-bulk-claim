package uk.gov.justice.laa.payments.submit.util;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MatterTypeUtil {

  private static final String DELIMITER = "[+:]";
  private static final String PART_SUFFIX = "#";

  public static final String MATTER_TYPE_CODE = "claim.matterTypeCode";

  public static final int FIRST_PART = 0;
  public static final int SECOND_PART = 1;

  public static String part(String matterTypeCode, int part) {
    if (matterTypeCode == null || matterTypeCode.isBlank()) {
      return null;
    }
    String[] split = matterTypeCode.split(DELIMITER);
    return part < split.length ? split[part] : null;
  }

  public static String partIdentifier(int part) {
    return MATTER_TYPE_CODE + PART_SUFFIX + part;
  }

  public static Set<String> changedPartIdentifiers(String before, String after) {
    Set<String> identifiers = new LinkedHashSet<>();
    for (int part : new int[] {FIRST_PART, SECOND_PART}) {
      if (!Objects.equals(part(before, part), part(after, part))) {
        identifiers.add(partIdentifier(part));
      }
    }
    return identifiers;
  }
}
