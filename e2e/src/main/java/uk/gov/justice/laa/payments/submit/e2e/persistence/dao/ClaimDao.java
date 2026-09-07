package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ClaimDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ClaimDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      String id,
      String submissionId,
      int lineNumber,
      String scheduleReference,
      String caseReferenceNumber,
      String uniqueFileNumber,
      String matterTypeCode,
      String feeCode,
      String userId) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.claim (
          id, submission_id, status, line_number, schedule_reference, case_reference_number,
          unique_file_number, matter_type_code, fee_code, created_by_user_id, created_on
        ) VALUES (
          :id::uuid, :submissionId::uuid, 'VALID', :lineNumber, :scheduleReference,
          :caseReferenceNumber, :uniqueFileNumber, :matterTypeCode, :feeCode, :userId, now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("submissionId", submissionId)
            .addValue("lineNumber", lineNumber)
            .addValue("scheduleReference", scheduleReference)
            .addValue("caseReferenceNumber", caseReferenceNumber)
            .addValue("uniqueFileNumber", uniqueFileNumber)
            .addValue("matterTypeCode", matterTypeCode)
            .addValue("feeCode", feeCode)
            .addValue("userId", userId));
  }
}
