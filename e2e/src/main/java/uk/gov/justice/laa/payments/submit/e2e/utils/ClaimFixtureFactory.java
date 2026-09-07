package uk.gov.justice.laa.payments.submit.e2e.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.CalculatedFeeDetailDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ClaimCaseDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ClaimDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ClaimSummaryFeeDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ClientDao;

public class ClaimFixtureFactory {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ClaimFixtureFactory(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Used to split a total amount evenly across a total number of claims.
   * @param total the total amount to be split
   * @param totalClaims the number of claims to split the total amount across
   * @return a list of amounts split evenly across the specified number of claims
   */
  public static List<BigDecimal> splitEvenly(BigDecimal total, int totalClaims) {
    List<BigDecimal> amounts = new ArrayList<>();
    BigDecimal share = total.divide(BigDecimal.valueOf(totalClaims), 2, RoundingMode.HALF_UP);
    BigDecimal runningTotal = BigDecimal.ZERO;
    for (int i = 0; i < totalClaims - 1; i++) {
      amounts.add(share);
      runningTotal = runningTotal.add(share);
    }
    amounts.add(total.subtract(runningTotal));
    return amounts;
  }

  public UUID addClaim(
      UUID submissionId, int lineNumber, BigDecimal totalAmount, String userId) {
    UUID claimId =
        ClaimDao.builder(submissionId)
            .lineNumber(lineNumber)
            .scheduleReference("SCH" + lineNumber)
            .caseReferenceNumber("CASE" + lineNumber)
            .uniqueFileNumber("010126/00" + lineNumber)
            .userId(userId)
            .build().insert(jdbcTemplate);

    ClaimCaseDao.builder(claimId).userId(userId).build().insert(jdbcTemplate);
    ClientDao.builder(claimId).userId(userId).build().insert(jdbcTemplate);

    UUID claimSummaryFeeId =
        ClaimSummaryFeeDao.builder(claimId).userId(userId).build().insert(jdbcTemplate);

    CalculatedFeeDetailDao.builder(claimId, claimSummaryFeeId)
        .totalAmount(totalAmount)
        .userId(userId)
        .build()
        .insert(jdbcTemplate);

    return claimId;
  }
}
