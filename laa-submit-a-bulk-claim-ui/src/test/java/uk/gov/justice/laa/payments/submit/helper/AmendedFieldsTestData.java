package uk.gov.justice.laa.payments.submit.helper;

import java.util.Set;

public final class AmendedFieldsTestData {

  private AmendedFieldsTestData() {}

  public static Set<String> amended(String... fieldNames) {
    return Set.of(fieldNames);
  }
}
