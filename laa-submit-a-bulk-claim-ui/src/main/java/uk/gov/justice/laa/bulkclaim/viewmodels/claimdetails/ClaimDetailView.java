package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import java.util.LinkedHashMap;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;

public interface ClaimDetailView<K extends ClaimViewField<?>> {

  LinkedHashMap<K, Object> summaryRows();

  LinkedHashMap<K, Object> valueRows();

  LinkedHashMap<K, Object> totalRows();

  String pageTitle();

  default Object getUFN() {
    return summaryRows().get(ClaimDetailsViewField.UNIQUE_FILE_NUMBER);
  }

  default Object getFeeCode() {
    return summaryRows().get(ClaimDetailsViewField.FEE_CODE);
  }
}
