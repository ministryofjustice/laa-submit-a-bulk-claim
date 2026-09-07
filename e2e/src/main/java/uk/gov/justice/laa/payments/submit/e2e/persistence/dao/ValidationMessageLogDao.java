package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ValidationMessageLogDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ValidationMessageLogDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(String id, String submissionId, String claimId, String source, String displayMessage) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.validation_message_log (
          id, submission_id, claim_id, type, source, display_message, created_on
        ) VALUES (
                  :id::uuid, 
                  :submissionId::uuid,
                  :claimId::uuid, 
                  :type, 
                  :source, 
                  :displayMessage, 
                  now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("submissionId", submissionId)
            .addValue("claimId", claimId)
            .addValue("type", "WARNING")
            .addValue("source", source)
            .addValue("displayMessage", displayMessage));
  }
}
