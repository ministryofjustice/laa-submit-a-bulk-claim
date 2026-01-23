package uk.gov.justice.laa.bulkclaim;

import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class AbstractPactTest {
  public static final String CONSUMER = "laa-submit-a-bulk-claim";
  public static final String PROVIDER = "laa-data-claims-api";

  protected static final String UUID_REGEX =
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

  protected static final String ANY_FORMAT_REGEX =
      "([a-zA-Z0-9 !\"£$%^&*()_+\\-=\\[\\]{};'#:@~,./<>?\\\\|`¬]+)";
  protected static final String ANY_NUMBER_REGEX = "([0-9]+)";

  protected final List<String> userOffices = List.of("ABC123", "XYZ789");
  protected final UUID bulkSubmissionId = UUID.randomUUID();
  protected final UUID submissionId = UUID.randomUUID();
  protected final UUID claimId = UUID.randomUUID();
  protected final UUID matterStartId = UUID.randomUUID();

  @MockitoBean OAuth2AuthorizedClientManager authorizedClientManager;
}
