package uk.gov.justice.laa.payments.submit.e2e.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.justice.laa.payments.submit.e2e.config.EnvConfig;
import uk.gov.justice.laa.payments.submit.e2e.persistence.DatabaseQueryExecutor;
import uk.gov.justice.laa.payments.submit.e2e.utils.ClaimFixtureFactory;

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
