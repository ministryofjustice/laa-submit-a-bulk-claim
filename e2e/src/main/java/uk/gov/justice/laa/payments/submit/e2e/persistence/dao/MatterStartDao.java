package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class MatterStartDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public MatterStartDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      String id, String submissionId, String categoryCode, String mediationType, String userId) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.matter_start (
          id, submission_id, number_of_matter_starts, category_code, mediation_type,
          created_by_user_id, created_on
        ) VALUES (
                  :id::uuid, 
                  :submissionId::uuid, 
                  :numberOfMatterStarts, 
                  :categoryCode, 
                  :mediationType, 
                  :userId, 
                  now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("submissionId", submissionId)
            .addValue("numberOfMatterStarts", 1)
            .addValue("categoryCode", categoryCode)
            .addValue("mediationType", mediationType)
            .addValue("userId", userId));
  }
}
