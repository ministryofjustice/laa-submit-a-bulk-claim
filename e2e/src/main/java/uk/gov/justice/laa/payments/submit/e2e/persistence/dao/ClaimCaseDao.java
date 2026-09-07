package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ClaimCaseDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ClaimCaseDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(String id, String claimId, String userId) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.claim_case (id, claim_id, created_by_user_id, created_on)
        VALUES (
                :id::uuid, 
                :claimId::uuid, 
                :userId, 
                now())
        """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("claimId", claimId)
            .addValue("userId", userId));
  }
}
