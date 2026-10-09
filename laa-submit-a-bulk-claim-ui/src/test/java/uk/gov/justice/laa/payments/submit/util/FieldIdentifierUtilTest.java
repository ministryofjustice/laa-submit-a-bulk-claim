package uk.gov.justice.laa.payments.submit.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Field identifier util test")
class FieldIdentifierUtilTest {

  @ParameterizedTest
  @CsvSource({
    "claimSummaryFee.netProfitCostsAmount, net_profit_costs_amount",
    "fee.totalAmount, total_amount",
    "client.client2Forename, client2_forename",
    "claim.matterTypeCode#1, matter_type_code#1",
    "net_profit_costs_amount, net_profit_costs_amount",
    "totalAmount, total_amount"
  })
  @DisplayName("Should flatten identifiers to the form returned by the claims API")
  void shouldFlattenIdentifier(String identifier, String expected) {
    assertThat(FieldIdentifierUtil.toApiIdentifier(identifier)).isEqualTo(expected);
  }

  @org.junit.jupiter.api.Test
  @DisplayName("Should return null for a null identifier")
  void shouldReturnNullForNull() {
    assertThat(FieldIdentifierUtil.toApiIdentifier(null)).isNull();
  }
}
