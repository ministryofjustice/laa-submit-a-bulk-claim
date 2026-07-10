package uk.gov.justice.laa.bulkclaim.e2e.helpers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PageHelper {

  public static Locator cardByTitle(String title, Page page) {
    return page.locator(".govuk-summary-card")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator("h2.govuk-summary-card__title")
                        .filter(new Locator.FilterOptions().setHasText(title))))
        .first();
  }

  public static Locator tableByCard(Locator card) {
    return card.locator("table.govuk-table");
  }

  public static Locator summaryListByCard(Locator card) {
    return card.locator("dl.govuk-summary-list");
  }

  public static Locator tableRowByLabel(Locator card, String label) {
    Locator table = tableByCard(card);
    return table.getByRole(AriaRole.ROW, new Locator.GetByRoleOptions().setName(label)).first();
  }
}
