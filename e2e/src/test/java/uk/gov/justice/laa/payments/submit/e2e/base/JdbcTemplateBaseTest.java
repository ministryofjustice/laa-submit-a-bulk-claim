package uk.gov.justice.laa.payments.submit.e2e.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.justice.laa.payments.submit.e2e.config.EnvConfig;
import uk.gov.justice.laa.payments.submit.e2e.persistence.DatabaseQueryExecutor;
import uk.gov.justice.laa.payments.submit.e2e.utils.ClaimFixtureFactory;

/**
 * Clears and seeds the database with test data before each test. 
 * 
 * <p>When inserting data into the DB, it should be done in the following order:</p>
 * <ol>
 *   <li>{@code claims.bulk_submission}</li>
 *   <li>{@code claims.submission}</li>
 *   <li>{@code claims.claim}</li>
 *   <li>{@code claims.claim_case}</li>
 *   <li>{@code claims.claim_amendment}</li>
 *   <li>{@code claims.client}</li>
 *   <li>{@code claims.claim_summary_fee}</li>
 *   <li>{@code claims.calculated_fee_detail}</li>
 *   <li>{@code claims.assessment}</li>
 *   <li>{@code claims.matter_start}</li>
 *   <li>{@code claims.validation_message_log}</li>
 * </ol>
 * 
 */
public abstract class JdbcTemplateBaseTest extends BaseTest {

  protected NamedParameterJdbcTemplate jdbcTemplate;
  protected ClaimFixtureFactory claimFixtureFactory;

  protected abstract void seedDatabase();

  @Override
  @BeforeEach
  public void setup() {
    dqe = new DatabaseQueryExecutor();
    dqe.cleanAll();

    jdbcTemplate = dqe.namedParameterJdbcTemplate();
    claimFixtureFactory = new ClaimFixtureFactory(jdbcTemplate);

    seedDatabase();

    page = BrowserSession.getContext().newPage();
    page.navigate(EnvConfig.baseUrl());
  }
}
