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
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.MatterStartDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ValidationMessageLogDao;

public class ClaimFixtureFactory {

  private final ClaimDao claimDao;
  private final ClaimCaseDao claimCaseDao;
  private final ClientDao clientDao;
  private final ClaimSummaryFeeDao claimSummaryFeeDao;
  private final CalculatedFeeDetailDao calculatedFeeDetailDao;
  private final ValidationMessageLogDao validationMessageLogDao;
  private final MatterStartDao matterStartDao;

  public ClaimFixtureFactory(NamedParameterJdbcTemplate jdbcTemplate) {
    this.claimDao = new ClaimDao(jdbcTemplate);
    this.claimCaseDao = new ClaimCaseDao(jdbcTemplate);
    this.clientDao = new ClientDao(jdbcTemplate);
    this.claimSummaryFeeDao = new ClaimSummaryFeeDao(jdbcTemplate);
    this.calculatedFeeDetailDao = new CalculatedFeeDetailDao(jdbcTemplate);
    this.validationMessageLogDao = new ValidationMessageLogDao(jdbcTemplate);
    this.matterStartDao = new MatterStartDao(jdbcTemplate);
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

  public String addClaim(
      String submissionId, int lineNumber, BigDecimal totalAmount, String userId) {
    String claimId = UUID.randomUUID().toString();
    String claimSummaryFeeId = UUID.randomUUID().toString();

    claimDao.insert(
        claimId,
        submissionId,
        lineNumber,
        "SCH" + lineNumber,
        "CASE" + lineNumber,
        "010126/00" + lineNumber,
        "TEST",
        "FEE1",
        userId);
    claimCaseDao.insert(UUID.randomUUID().toString(), claimId, userId);
    clientDao.insert(UUID.randomUUID().toString(), claimId, userId);
    claimSummaryFeeDao.insert(claimSummaryFeeId, claimId, userId);
    calculatedFeeDetailDao.insert(
        UUID.randomUUID().toString(), claimSummaryFeeId, claimId, totalAmount, userId);

    return claimId;
  }

  public void addWarning(String submissionId, String claimId, int index) {
    validationMessageLogDao.insert(
        UUID.randomUUID().toString(),
        submissionId,
        claimId,
        "VALIDATOR",
        "Test warning message " + index);
  }

  public void addMatterStart(
      String submissionId, String categoryCode, String mediationType, String userId) {
    matterStartDao.insert(
        UUID.randomUUID().toString(), submissionId, categoryCode, mediationType, userId);
  }
}
