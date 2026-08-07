package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.Locale;
import java.util.function.Function;
import org.springframework.context.MessageSource;

public interface ClaimViewField<T> {

  String LABEL_KEY_PREFIX = "claimDetail.rows.";

  String name();

  Function<T, Object> getAccessor();

  default String label(MessageSource messageSource) {
    return messageSource.getMessage(LABEL_KEY_PREFIX + name(), null, name(), Locale.UK);
  }
}
