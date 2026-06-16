package uk.gov.justice.laa.bulkclaim.ui.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;

/** Base class for Playwright UI flow tests. */
public abstract class AbstractUiTest extends AbstractAccessibilityTest {

  @Override
  protected Path writeFile(String fileName, String content) throws IOException {
    Path file = Path.of("build", "tmp", "ui", fileName);
    Files.createDirectories(file.getParent());
    Files.writeString(file, content);
    return file;
  }
}

