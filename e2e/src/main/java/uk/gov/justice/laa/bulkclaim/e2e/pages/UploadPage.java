package uk.gov.justice.laa.bulkclaim.e2e.pages;

import static com.microsoft.playwright.options.AriaRole.LINK;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class UploadPage extends BasePage {

  private final Locator createNilSubmissionButton;

  public UploadPage(Page page) {
    super(page, "Upload a bulk claim file");

    createNilSubmissionButton =
        page.getByRole(LINK, new Page.GetByRoleOptions().setName("Create nil submission"));
  }
}
