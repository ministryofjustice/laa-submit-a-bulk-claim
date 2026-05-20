package uk.gov.justice.laa.bulkclaim.e2e.steps.claim;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Map;
import uk.gov.justice.laa.bulkclaim.e2e.pages.ClaimDetailPage;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;

public class ClaimDetailSteps extends BaseUiSteps {

  @Then("I should see the following fee calculation headings:")
  public void iShouldSeeTheFollowingFeeCalculationHeadings(DataTable table) {
    List<String> headings = table.asMaps().stream().map(r -> r.getOrDefault("Heading", "")).toList();
    claimDetailPage().assertFeeHeadingsPresent(headings);
  }

  @Then("the fee calculation should show the following values")
  @Then("the crime fee calculation should show the following values")
  public void theFeeCalculationShouldShowTheFollowingValues(DataTable table) {
    assertFeeCalculationRows(table);
  }

  @Then("the fee calculation should show the entered and calculated values correctly")
  public void theFeeCalculationShouldShowTheEnteredAndCalculatedValuesCorrectly() {
    assertSpecificFeeRow("Net Disbursements", "£20.00", "£20.00");
    assertSpecificFeeRow("Disbursement VAT", "£10.50", "£10.50");
    assertSpecificFeeRow("Total", null, "£269.50");
  }

  @Then("the crime fee calculation should reflect the entered values")
  public void theCrimeFeeCalculationShouldReflectTheEnteredValues() {
    Map<String, String> expectedCrimeClaim = uk.gov.justice.laa.bulkclaim.e2e.state.TestContext.current().get("expectedCrimeClaim");
    if (expectedCrimeClaim == null || expectedCrimeClaim.isEmpty()) {
      throw new AssertionError("No expected crime claim found in test context");
    }

    String netProfitCosts = expectedCrimeClaim.get("netProfitCosts");
    String netTravelCosts = expectedCrimeClaim.get("netTravelCosts");
    String netWaitingCosts = expectedCrimeClaim.get("netWaitingCosts");
    String netDisbursementAmount = expectedCrimeClaim.get("netDisbursementAmount");
    String disbursementVatAmount = expectedCrimeClaim.get("disbursementVatAmount");
    String expectedTotal = expectedCrimeClaim.get("expectedTotal");

    if (netProfitCosts != null && !netProfitCosts.isBlank()) {
      assertSpecificFeeRow("Net Profit Cost", normalizeCurrency(netProfitCosts), null);
    }
    if (netDisbursementAmount != null && !netDisbursementAmount.isBlank()) {
      assertSpecificFeeRow("Net Disbursements", normalizeCurrency(netDisbursementAmount), null);
    }
    if (disbursementVatAmount != null && !disbursementVatAmount.isBlank()) {
      assertSpecificFeeRow("Disbursement VAT", normalizeCurrency(disbursementVatAmount), null);
    }
    if ((netTravelCosts != null && !netTravelCosts.isBlank()) || (netWaitingCosts != null && !netWaitingCosts.isBlank())) {
      double totalTravel = parseNumberOrZero(netTravelCosts) + parseNumberOrZero(netWaitingCosts);
      assertSpecificFeeRow(
          "Travel and Waiting",
          normalizeCurrency(String.format(java.util.Locale.ROOT, "%.2f", totalTravel)),
          null);
    }
    if (expectedTotal != null && !expectedTotal.isBlank()) {
      assertSpecificFeeRow("Total", null, expectedTotal.startsWith("£") ? expectedTotal : normalizeCurrency(expectedTotal));
    }
  }

  @Then("the claim calculated value should be {string}")
  public void theClaimCalculatedValueShouldBe(String expectedValue) {
    String body = page().locator("body").innerText();
    if (!body.contains(expectedValue)) {
      throw new AssertionError("Expected claim calculated value: " + expectedValue);
    }
  }

  @Then("I should see a VOIDED tag under the View link for claim {int}")
  public void iShouldSeeAVoidedTagUnderTheViewLinkForClaim(int claimNumber) {
    summaryPage().expectVoidedTagForClaim(Math.max(claimNumber - 1, 0));
  }

  @Then("I should see a voided claim banner on the fee calculation screen")
  public void iShouldSeeAVoidedClaimBannerOnTheFeeCalculationScreen() {
    claimDetailPage().expectVoidedBanner();
  }

  private void assertFeeCalculationRows(DataTable table) {
    if (table == null) {
      throw new PendingException("Expected DataTable for fee calculation rows");
    }
    for (Map<String, String> row : table.asMaps(String.class, String.class)) {
      String item = row.get("Item");
      if (item == null || item.isBlank()) {
        continue;
      }
      ClaimDetailPage.FeeCalculationRow feeRow = claimDetailPage().getFeeCalculationRow(item);
      if (feeRow == null) {
        throw new AssertionError("Missing fee calculation row: " + item);
      }
      String entered = normalizeCurrency(row.get("Entered"));
      String calculated = normalizeCurrency(row.get("Calculated"));
      if (entered != null && !entered.isBlank() && !entered.equals(feeRow.entered())) {
        throw new AssertionError(
            "Unexpected entered value for "
                + item
                + ": expected "
                + entered
                + " got "
                + feeRow.entered());
      }
      if (calculated != null && !calculated.isBlank() && !calculated.equals(feeRow.calculated())) {
        throw new AssertionError(
            "Unexpected calculated value for "
                + item
                + ": expected "
                + calculated
                + " got "
                + feeRow.calculated());
      }
    }
  }

  private void assertSpecificFeeRow(
      String label, String expectedEntered, String expectedCalculated) {
    ClaimDetailPage.FeeCalculationRow row = claimDetailPage().getFeeCalculationRow(label);
    if (row == null) {
      throw new AssertionError("Missing fee calculation row: " + label);
    }
    if (expectedEntered != null && !expectedEntered.equals(row.entered())) {
      throw new AssertionError(
          "Unexpected entered value for "
              + label
              + ": expected "
              + expectedEntered
              + " got "
              + row.entered());
    }
    if (expectedCalculated != null && !expectedCalculated.equals(row.calculated())) {
      throw new AssertionError(
          "Unexpected calculated value for "
              + label
              + ": expected "
              + expectedCalculated
              + " got "
              + row.calculated());
    }
  }

  private String normalizeCurrency(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isBlank()) {
      return trimmed;
    }
    if (trimmed.startsWith("£")) {
      return trimmed;
    }
    return "£"
        + String.format(
            java.util.Locale.ROOT, "%.2f", Double.parseDouble(trimmed.replace(",", "")));
  }

  private double parseNumberOrZero(String value) {
    if (value == null || value.isBlank()) {
      return 0d;
    }
    return Double.parseDouble(value.replace("£", "").replace(",", "").trim());
  }
}

