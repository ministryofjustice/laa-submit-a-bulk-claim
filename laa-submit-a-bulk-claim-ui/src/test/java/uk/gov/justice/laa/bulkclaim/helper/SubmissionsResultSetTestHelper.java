package uk.gov.justice.laa.bulkclaim.helper;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.NonNull;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

public class SubmissionsResultSetTestHelper {

  public static @NonNull SubmissionsResultSet getSubmissionsResultSet(int noOfPeriods) {

    List<SubmissionBase> content = new ArrayList<>();
    YearMonth ym = YearMonth.now().minusMonths(1);
    for (int i = 0; i < noOfPeriods; i++) {
      SubmissionBase s1 = new SubmissionBase();
      content.add(
          s1.submissionPeriod(
              ym.format(DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH)).toUpperCase()));
      ym = ym.minusMonths(1);
    }

    final SubmissionsResultSet response = new SubmissionsResultSet();
    response.setContent(content);
    response.setTotalElements(noOfPeriods);
    response.setNumber(0);
    response.setSize(12);
    response.setTotalPages(1);
    return response;
  }
}
