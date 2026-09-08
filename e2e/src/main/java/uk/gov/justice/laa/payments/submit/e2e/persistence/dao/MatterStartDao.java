package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class MatterStartDao {

  @Default
  private UUID id = UUID.randomUUID();
  private final UUID submissionId;
  private String categoryCode;
  private String mediationType;
  private String userId;
  @Default
  private int numberOfMatterStarts = 1;

  public static MatterStartDaoBuilder builder(UUID submissionId) {
    return new MatterStartDaoBuilder().submissionId(submissionId);
  }

  public UUID insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.matter_start (
          id, submission_id, number_of_matter_starts, category_code, mediation_type,
          created_by_user_id, created_on
        ) VALUES (
                  :id,
                  :submissionId,
                  :numberOfMatterStarts,
                  :categoryCode,
                  :mediationType,
                  :userId,
                  now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("submissionId", this.submissionId)
            .addValue("numberOfMatterStarts", this.numberOfMatterStarts)
            .addValue("categoryCode", this.categoryCode)
            .addValue("mediationType", this.mediationType)
            .addValue("userId", this.userId));
    return this.id;
  }
}
