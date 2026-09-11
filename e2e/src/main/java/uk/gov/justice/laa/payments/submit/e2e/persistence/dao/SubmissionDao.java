package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class SubmissionDao {

  @Default
  private UUID id = UUID.randomUUID();
  private final UUID bulkSubmissionId;
  @Default
  private String officeAccountNumber = "0P322F";
  private String submissionPeriod;
  private String areaOfLaw;
  private int numberOfClaims;
  @Default
  private String userId = "test-user";
  @Default
  private String status = "VALIDATION_SUCCEEDED";
  @Default
  private boolean isNilSubmission = false;

  public static SubmissionDaoBuilder builder(UUID bulkSubmissionId) {
    return new SubmissionDaoBuilder().bulkSubmissionId(bulkSubmissionId);
  }

  public UUID insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.submission (
          id, bulk_submission_id, office_account_number, submission_period, area_of_law,
          status, is_nil_submission, number_of_claims, created_by_user_id, created_on,
          provider_user_id
        ) VALUES (
                  :id, 
                  :bulkSubmissionId, 
                  :officeAccountNumber, 
                  :submissionPeriod,
                  :areaOfLaw, 
                  :status, 
                  :isNilSubmission, 
                  :numberOfClaims, 
                  :userId, 
                  now(), 
                  :userId
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("bulkSubmissionId", this.bulkSubmissionId)
            .addValue("officeAccountNumber", this.officeAccountNumber)
            .addValue("submissionPeriod", this.submissionPeriod)
            .addValue("areaOfLaw", this.areaOfLaw)
            .addValue("status", this.status)
            .addValue("isNilSubmission", this.isNilSubmission)
            .addValue("numberOfClaims", this.numberOfClaims)
            .addValue("userId", this.userId));
    return this.id;
  }
}
