package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.context.MessageSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;

public interface ClaimViewField<T> {

  String LABEL_KEY_PREFIX = "claimDetail.rows.";

  String name();

  Function<T, Object> getAccessor();

  default String label(MessageSource messageSource) {
    return messageSource.getMessage(LABEL_KEY_PREFIX + name(), null, name(), Locale.UK);
  }

  @SuppressWarnings("unchecked")
  static ClaimViewField<CrimeLowerClaimDetails> asCrimeLowerField(
      ClaimViewField<? extends ClaimDetails> field) {
    return (ClaimViewField<CrimeLowerClaimDetails>) (ClaimViewField<?>) field;
  }

  @SuppressWarnings("unchecked")
  static ClaimViewField<LegalHelpClaimDetails> asLegalHelpField(
      ClaimViewField<? extends ClaimDetails> field) {
    return (ClaimViewField<LegalHelpClaimDetails>) (ClaimViewField<?>) field;
  }

  @SuppressWarnings("unchecked")
  static ClaimViewField<MediationClaimDetails> asMediationField(
      ClaimViewField<? extends ClaimDetails> field) {
    return (ClaimViewField<MediationClaimDetails>) (ClaimViewField<?>) field;
  }

  static <K extends ClaimViewField<T>, T> LinkedHashMap<K, Object> toFieldMap(
      Stream<K> fields, T claim) {
    LinkedHashMap<K, Object> fieldMap = new LinkedHashMap<>();
    fields.forEach(field -> fieldMap.put(field, field.getAccessor().apply(claim)));
    return fieldMap;
  }
}
