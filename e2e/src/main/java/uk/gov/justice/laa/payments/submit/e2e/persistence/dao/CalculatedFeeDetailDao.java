package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.math.BigDecimal;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class CalculatedFeeDetailDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public CalculatedFeeDetailDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      String id,
      String claimSummaryFeeId,
      String claimId,
      BigDecimal totalAmount,
      String userId) {
    jdbcTemplate.update(
        """
            INSERT INTO claims.calculated_fee_detail (
              id, claim_summary_fee_id, claim_id, total_amount, created_by_user_id, created_on
            ) VALUES (
                      :id::uuid, 
                      :claimSummaryFeeId::uuid, 
                      :claimId::uuid, 
                      :totalAmount, 
                      :userId, 
                      now()
            )
            """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("claimSummaryFeeId", claimSummaryFeeId)
            .addValue("claimId", claimId)
            .addValue("totalAmount", totalAmount)
            .addValue("userId", userId));
  }
}
