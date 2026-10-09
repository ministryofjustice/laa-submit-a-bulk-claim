package uk.gov.justice.laa.payments.submit.helper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.justice.laa.payments.submit.util.FieldIdentifierUtil;

public final class AmendedFieldsTestData {

  private AmendedFieldsTestData() {}

  /** Builds the amended field set as the claims API returns it: flattened snake_case names. */
  public static Set<String> amended(String... fieldNames) {
    return Arrays.stream(fieldNames)
        .map(FieldIdentifierUtil::toApiIdentifier)
        .collect(Collectors.toSet());
  }
}
