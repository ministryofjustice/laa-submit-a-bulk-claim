package uk.gov.justice.laa.payments.submit.util;

import java.util.Locale;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * The claims API returns amendment field identifiers flattened to the final segment in snake_case
 * (e.g. {@code claimSummaryFee.netProfitCostsAmount} becomes {@code net_profit_costs_amount}). This
 * applies the same transformation so identifiers can be compared on either form.
 */
@UtilityClass
public class FieldIdentifierUtil {

  private static final Pattern CAMEL_CASE_BOUNDARY = Pattern.compile("([a-z0-9])([A-Z])");

  public static String toApiIdentifier(String identifier) {
    if (identifier == null) {
      return null;
    }
    String[] segments = identifier.split("\\.");
    return CAMEL_CASE_BOUNDARY
        .matcher(segments[segments.length - 1])
        .replaceAll("$1_$2")
        .toLowerCase(Locale.ROOT);
  }
}
