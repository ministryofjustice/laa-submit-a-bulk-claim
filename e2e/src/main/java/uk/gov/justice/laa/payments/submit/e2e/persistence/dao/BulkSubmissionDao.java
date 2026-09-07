package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class BulkSubmissionDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public BulkSubmissionDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(String id, String userId) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.bulk_submission (id, data, status, created_by_user_id, created_on)
        VALUES (
                :id::uuid, 
                -- This contains usually a full JSON object, it's not needed for SaBC so leave this blank
                '{}'::jsonb, 
                :status, 
                :userId, 
                now())
        """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("status", "VALIDATION_SUCCEEDED")
            .addValue("userId", userId));
  }
}
