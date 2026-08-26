package uk.gov.justice.laa.payments.submit.viewmodels.claimdetails;

import java.util.LinkedHashMap;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimViewField;

public interface ClaimDetailView<K extends ClaimViewField<?>> {

  LinkedHashMap<K, Object> summaryRows();

  LinkedHashMap<K, Object> valueRows();

  LinkedHashMap<K, Object> totalRows();

  String pageTitle();

  default Object getUfn() {
    return summaryRows().get(ClaimDetailsViewField.UNIQUE_FILE_NUMBER);
  }

  default Object getFeeCode() {
    return summaryRows().get(ClaimDetailsViewField.FEE_CODE);
  }
}
