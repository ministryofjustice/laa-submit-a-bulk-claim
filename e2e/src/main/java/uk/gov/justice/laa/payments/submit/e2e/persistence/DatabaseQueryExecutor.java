package uk.gov.justice.laa.payments.submit.e2e.persistence;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.justice.laa.payments.submit.e2e.config.EnvConfig;

public class DatabaseQueryExecutor {

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public DatabaseQueryExecutor() {
    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(EnvConfig.dbConnectionUrl());
    dataSource.setUser(EnvConfig.dbUser());
    dataSource.setPassword(EnvConfig.dbPassword());
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
    return namedParameterJdbcTemplate;
  }

  public void cleanAll() {
    deleteAll("validation_message_log");
    deleteAll("assessment");
    deleteAll("calculated_fee_detail");
    deleteAll("claim_summary_fee");
    deleteAll("client");
    deleteAll("claim_amendment");
    deleteAll("claim_case");
    deleteAll("claim");
    deleteAll("matter_start");
    deleteAll("submission");
    deleteAll("bulk_submission");
  }

  public void deleteAll(String table) {
    jdbcTemplate.update(String.format("DELETE FROM claims.%s", table));
  }
}
