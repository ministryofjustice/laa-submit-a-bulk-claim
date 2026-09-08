package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class BulkSubmissionDao {

  @Default
  @Getter
  private UUID id = UUID.randomUUID();
  @Default
  private String userId = "test-user";
  @Default
  private String status = "VALIDATION_SUCCEEDED";

  public UUID insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.bulk_submission (id, data, status, created_by_user_id, created_on)
        VALUES (
                :id, 
                -- This contains usually a full JSON object, it's not needed for SaBC so leave this blank
                '{}'::jsonb, 
                :status, 
                :userId, 
                now())
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("status", this.status)
            .addValue("userId", this.userId));
    return this.id;
  }
}
