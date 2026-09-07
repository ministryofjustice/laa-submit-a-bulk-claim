package uk.gov.justice.laa.payments.submit.e2e.persistence.dao;

import java.util.UUID;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Builder
public class ClaimDao {

  @Default
  @Getter
  private UUID id = UUID.randomUUID();
  private final UUID submissionId;
  private int lineNumber;
  private String scheduleReference;
  private String caseReferenceNumber;
  private String uniqueFileNumber;
  @Default
  private String matterTypeCode = "TEST";
  @Default
  private String feeCode = "FEE1";
  private String userId;
  @Default
  private String status = "VALID";

  public static ClaimDaoBuilder builder(UUID submissionId) {
    return new ClaimDaoBuilder().submissionId(submissionId);
  }

  public UUID insert(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        """
        INSERT INTO claims.claim (
          id, submission_id, status, line_number, schedule_reference, case_reference_number,
          unique_file_number, matter_type_code, fee_code, created_by_user_id, created_on
        ) VALUES (
                  :id,
                  :submissionId,
                  :status,
                  :lineNumber,
                  :scheduleReference,
                  :caseReferenceNumber,
                  :uniqueFileNumber,
                  :matterTypeCode,
                  :feeCode,
                  :userId,
                  now()
        )
        """,
        new MapSqlParameterSource()
            .addValue("id", this.id)
            .addValue("submissionId", this.submissionId)
            .addValue("status", this.status)
            .addValue("lineNumber", this.lineNumber)
            .addValue("scheduleReference", this.scheduleReference)
            .addValue("caseReferenceNumber", this.caseReferenceNumber)
            .addValue("uniqueFileNumber", this.uniqueFileNumber)
            .addValue("matterTypeCode", this.matterTypeCode)
            .addValue("feeCode", this.feeCode)
            .addValue("userId", this.userId));
    return this.id;
  }
}
