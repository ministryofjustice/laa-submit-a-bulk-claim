package uk.gov.justice.laa.bulkclaim;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class AbstractPactTest {
  protected static final String CONSUMER = "laa-submit-a-bulk-claim";
  protected static final String PROVIDER = "laa-data-claim-api";

  @MockitoBean
  OAuth2AuthorizedClientManager authorizedClientManager;


  protected static String readJsonFromFile(final String fileName) throws Exception {
    Path path = Paths.get("src/pactTest/resources/responses/", fileName);
    return Files.readString(path);
  }
}
