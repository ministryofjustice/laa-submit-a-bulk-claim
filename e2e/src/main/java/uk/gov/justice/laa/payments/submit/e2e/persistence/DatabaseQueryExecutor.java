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
    jdbcTemplate.update("DELETE FROM claims.validation_message_log");
    jdbcTemplate.update("DELETE FROM claims.matter_start");
    jdbcTemplate.update("DELETE FROM claims.assessment");
    jdbcTemplate.update("DELETE FROM claims.calculated_fee_detail");
    jdbcTemplate.update("DELETE FROM claims.claim_summary_fee");
    jdbcTemplate.update("DELETE FROM claims.client");
    jdbcTemplate.update("DELETE FROM claims.claim_amendment");
    jdbcTemplate.update("DELETE FROM claims.claim_case");
    jdbcTemplate.update("DELETE FROM claims.claim");
    jdbcTemplate.update("DELETE FROM claims.submission");
    jdbcTemplate.update("DELETE FROM claims.bulk_submission");
  }
}
