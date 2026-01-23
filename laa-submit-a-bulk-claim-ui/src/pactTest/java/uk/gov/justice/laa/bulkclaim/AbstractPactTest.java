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

  protected static final List<String> USER_OFFICES = List.of("ABC123", "XYZ789");
  protected static final UUID BULK_SUBMISSION_ID = UUID.randomUUID();
  protected static final UUID SUBMISSION_ID = UUID.randomUUID();
  protected static final UUID CLAIM_ID = UUID.randomUUID();
  protected static final UUID MATTER_START_ID = UUID.randomUUID();

  @MockitoBean OAuth2AuthorizedClientManager authorizedClientManager;
}
