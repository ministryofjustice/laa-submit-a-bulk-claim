package uk.gov.justice.laa.payments.submit.e2e.base;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.justice.laa.payments.submit.e2e.config.EnvConfig;
import uk.gov.justice.laa.payments.submit.e2e.models.Insert;
import uk.gov.justice.laa.payments.submit.e2e.persistence.DatabaseQueryExecutor;

public abstract class SqlInsertBaseTest extends BaseTest {

  protected abstract List<Insert> inserts();

  @Override
  @BeforeEach
  public void setup() {
    try {
      dqe = new DatabaseQueryExecutor();
      dqe.cleanAll();
      dqe.seed(inserts());
    } catch (SQLException e) {
      throw new RuntimeException("Failed to seed database", e);
    }

    page = BrowserSession.getContext().newPage();
    page.navigate(EnvConfig.baseUrl());
  }
}
