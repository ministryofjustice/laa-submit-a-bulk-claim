package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class CalculatedFeeDetailDao {

  @Default
  private UUID id = UUID.randomUUID();
  private final UUID claimSummaryFeeId;
  private final UUID claimId;
  private BigDecimal totalAmount;
  private String userId;

  public static CalculatedFeeDetailDaoBuilder builder(UUID claimId, UUID claimSummaryFeeId) {
    return new CalculatedFeeDetailDaoBuilder().claimId(claimId).claimSummaryFeeId(claimSummaryFeeId);
  }

  public UUID insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.calculated_fee_detail (
          id, claim_summary_fee_id, claim_id, total_amount, created_by_user_id, created_on
        ) VALUES (
                  :id,
                  :claimSummaryFeeId,
                  :claimId,
                  :totalAmount,
                  :userId,
                  now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("claimSummaryFeeId", this.claimSummaryFeeId)
            .addValue("claimId", this.claimId)
            .addValue("totalAmount", this.totalAmount)
            .addValue("userId", this.userId));
    return this.id;
  }
}
