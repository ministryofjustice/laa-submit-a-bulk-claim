package uk.gov.justice.laa.bulkclaim.e2e.pages;

import static com.microsoft.playwright.options.AriaRole.BUTTON;
import static com.microsoft.playwright.options.AriaRole.LINK;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.nio.file.Path;
import lombok.Getter;

@Getter
public class UploadPage extends BasePage {

  private final Locator createNilSubmissionButton;
  private final Locator continueButton;
  private final Locator fileInput;

  public UploadPage(Page page) {
    super(page, "Upload a bulk claim file");

    createNilSubmissionButton =
        page.getByRole(LINK, new Page.GetByRoleOptions().setName("Create nil submission"));

    continueButton =
        page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));

    fileInput = page.locator("#file-input");
  }

  public void uploadFile(Path filePath) {
    fileInput.setInputFiles(filePath);
  }
}
