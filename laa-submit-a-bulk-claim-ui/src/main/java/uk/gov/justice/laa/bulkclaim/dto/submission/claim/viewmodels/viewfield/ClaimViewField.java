package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.Locale;
import java.util.function.Function;
import org.springframework.context.MessageSource;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public interface ClaimViewField<T> {

  String LABEL_KEY_PREFIX = "claimDetail.rows.";

  String name();

  Function<T, Object> getAccessor();

  /** Reads this field's Current Calculated value from the latest assessment, where applicable. */
  default Function<AssessmentGet, Object> getAssessmentAccessor() {
    return null;
  }

  default String label(MessageSource messageSource) {
    return messageSource.getMessage(LABEL_KEY_PREFIX + name(), null, name(), Locale.UK);
  }
}
