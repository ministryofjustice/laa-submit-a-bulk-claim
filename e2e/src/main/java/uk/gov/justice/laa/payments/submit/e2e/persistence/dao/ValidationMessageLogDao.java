package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class ValidationMessageLogDao {

  @Default
  private UUID id = UUID.randomUUID();
  private UUID submissionId = UUID.randomUUID();
  // Default this to null, submission errors don't have a claim ID.
  private UUID claimId;
  @Default
  private String source = "VALIDATOR";
  private String displayMessage;
  @Default
  private String type = "WARNING";

  public static ValidationMessageLogDaoBuilder builder(UUID submissionId) {
    return new ValidationMessageLogDaoBuilder().submissionId(submissionId);
  }

  public void insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.validation_message_log (
          id, submission_id, claim_id, type, source, display_message, created_on
        ) VALUES (
                  :id,
                  :submissionId,
                  :claimId,
                  :type,
                  :source,
                  :displayMessage,
                  now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("submissionId", this.submissionId)
            .addValue("claimId", this.claimId)
            .addValue("type", this.type)
            .addValue("source", this.source)
            .addValue("displayMessage", this.displayMessage));
  }
}
