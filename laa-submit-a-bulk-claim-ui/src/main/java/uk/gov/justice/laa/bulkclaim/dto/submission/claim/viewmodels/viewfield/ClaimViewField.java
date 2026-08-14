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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public interface ClaimViewField<T> {

  String LABEL_KEY_PREFIX = "claimDetail.rows.";

  String name();

  default Function<T, Object> getReportedAndCalculatedAccessor() {
    return null;
  }

  /** Reads this field's Current Calculated value from the latest assessment, where applicable. */
  default Function<AssessmentGet, Object> getCurrentCalculatedAccessor() {
    return null;
  }

  default Function<T, Object> getAccessor() {
    return getReportedAndCalculatedAccessor();
  }

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
