package uk.gov.justice.laa.payments.submit.e2e.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.gov.justice.laa.payments.submit.e2e.models.CalculatedFeeDetailInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.ClaimCaseInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.ClaimInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.ClaimSummaryFeeInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.ClientInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.Insert;
import uk.gov.justice.laa.payments.submit.e2e.models.MatterStartInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.ValidationMessageLogInsert;

public final class ClaimFixtureFactory {

  private ClaimFixtureFactory() {}

  public static List<BigDecimal> splitEvenly(BigDecimal total, int count) {
    List<BigDecimal> amounts = new ArrayList<>();
    BigDecimal share = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    BigDecimal runningTotal = BigDecimal.ZERO;
    for (int i = 0; i < count - 1; i++) {
      amounts.add(share);
      runningTotal = runningTotal.add(share);
    }
    amounts.add(total.subtract(runningTotal));
    return amounts;
  }

  public static String addClaim(
      List<Insert> inserts,
      String submissionId,
      int lineNumber,
      BigDecimal totalAmount,
      String userId) {
    String claimId = UUID.randomUUID().toString();
    String claimSummaryFeeId = UUID.randomUUID().toString();

    inserts.add(
        ClaimInsert.builder()
            .id(claimId)
            .submissionId(submissionId)
            .lineNumber(lineNumber)
            .scheduleReference("SCH" + lineNumber)
            .caseReferenceNumber("CASE" + lineNumber)
            .uniqueFileNumber("010126/00" + lineNumber)
            .matterTypeCode("TEST")
            .feeCode("FEE1")
            .userId(userId)
            .build());
    inserts.add(ClaimCaseInsert.builder().id(UUID.randomUUID().toString()).claimId(claimId)
        .userId(userId).build());
    inserts.add(ClientInsert.builder().id(UUID.randomUUID().toString()).claimId(claimId)
        .userId(userId).build());
    inserts.add(
        ClaimSummaryFeeInsert.builder()
            .id(claimSummaryFeeId)
            .claimId(claimId)
            .userId(userId)
            .build());
    inserts.add(
        CalculatedFeeDetailInsert.builder()
            .id(UUID.randomUUID().toString())
            .claimSummaryFeeId(claimSummaryFeeId)
            .claimId(claimId)
            .totalAmount(totalAmount)
            .userId(userId)
            .build());

    return claimId;
  }

  public static void addWarning(
      List<Insert> inserts, String submissionId, String claimId, int index) {
    inserts.add(
        ValidationMessageLogInsert.builder()
            .id(UUID.randomUUID().toString())
            .submissionId(submissionId)
            .claimId(claimId)
            .source("VALIDATOR")
            .displayMessage("Test warning message " + index)
            .build());
  }

  public static void addMatterStart(
      List<Insert> inserts,
      String submissionId,
      String categoryCode,
      String mediationType,
      String userId) {
    inserts.add(
        MatterStartInsert.builder()
            .id(UUID.randomUUID().toString())
            .submissionId(submissionId)
            .categoryCode(categoryCode)
            .mediationType(mediationType)
            .userId(userId)
            .build());
  }
}
