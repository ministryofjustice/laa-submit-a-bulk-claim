package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class ClientDao {

  @Default
  private UUID id = UUID.randomUUID();
  private final UUID claimId;
  private String userId;

  public static ClientDaoBuilder builder(UUID claimId) {
    return new ClientDaoBuilder().claimId(claimId);
  }

  public UUID insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.client (id, claim_id, created_by_user_id, created_on)
        VALUES (
                :id, 
                :claimId, 
                :userId, 
                now())
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("claimId", this.claimId)
            .addValue("userId", this.userId));
    return this.id;
  }
}
