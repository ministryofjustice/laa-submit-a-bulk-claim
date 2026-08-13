package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import java.util.LinkedHashMap;
import java.util.Objects;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public interface ClaimDetailView<K extends ClaimViewField<?>> {

  LinkedHashMap<K, Object> summaryRows();

  LinkedHashMap<K, ClaimFieldRow> valueRows();

  LinkedHashMap<K, ClaimFieldRow> totalRows();

  String pageTitle();

  static Object getCurrentCalculatedAccessor(AssessmentGet currentAssessment, Object field) {
    if (Objects.isNull(currentAssessment)) {
      return null;
    }
    var accessor = ((ClaimViewField<?>) field).getCurrentCalculatedAccessor();
    return accessor == null ? null : accessor.apply(currentAssessment);
  }
}
