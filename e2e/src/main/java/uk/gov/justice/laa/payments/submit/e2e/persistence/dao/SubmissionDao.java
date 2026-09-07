package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SubmissionDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public SubmissionDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      String id,
      String bulkSubmissionId,
      String officeAccountNumber,
      String submissionPeriod,
      String areaOfLaw,
      int numberOfClaims,
      String userId) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.submission (
          id, bulk_submission_id, office_account_number, submission_period, area_of_law,
          status, is_nil_submission, number_of_claims, created_by_user_id, created_on,
          provider_user_id
        ) VALUES (
                  :id::uuid, 
                  :bulkSubmissionId::uuid, 
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
            .addValue("id", id)
            .addValue("bulkSubmissionId", bulkSubmissionId)
            .addValue("officeAccountNumber", officeAccountNumber)
            .addValue("submissionPeriod", submissionPeriod)
            .addValue("areaOfLaw", areaOfLaw)
            .addValue("status", "VALIDATION_SUCCEEDED")
            .addValue("isNilSubmission", false)
            .addValue("numberOfClaims", numberOfClaims)
            .addValue("userId", userId));
  }
}
