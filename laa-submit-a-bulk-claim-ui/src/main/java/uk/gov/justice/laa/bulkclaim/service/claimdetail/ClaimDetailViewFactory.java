package uk.gov.justice.laa.bulkclaim.service.claimdetail;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimValueRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.mapper.CrimeLowerClaimDetailsMapper;
import uk.gov.justice.laa.bulkclaim.mapper.LegalHelpClaimDetailsMapper;
import uk.gov.justice.laa.bulkclaim.mapper.MediationClaimDetailsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Component
@RequiredArgsConstructor
public class ClaimDetailViewFactory {

  private final CrimeLowerClaimDetailsMapper crimeLowerClaimDetailsMapper;
  private final LegalHelpClaimDetailsMapper legalHelpClaimDetailsMapper;
  private final MediationClaimDetailsMapper mediationClaimDetailsMapper;
  private final MessageSource messageSource;

  public ClaimDetailView build(ClaimResponseV2 claimResponse) {
    if (claimResponse.getAreaOfLaw() == null) {
      throw new IllegalArgumentException(
          "Claim %s has no area of law".formatted(claimResponse.getId()));
    }

    return switch (claimResponse.getAreaOfLaw()) {
      case CRIME_LOWER -> {
        var details = crimeLowerClaimDetailsMapper.toCrimeLowerClaimDetails(claimResponse);
        yield new ClaimDetailView.CrimeLower(
            details,
            buildRows(CrimeLowerClaimDetailsViewField.VALUE_ROWS, details),
            buildRows(CrimeLowerClaimDetailsViewField.TOTAL_ROWS, details));
      }
      case LEGAL_HELP -> {
        var details = legalHelpClaimDetailsMapper.toLegalHelpClaimDetails(claimResponse);
        yield new ClaimDetailView.LegalHelp(
            details,
            buildRows(LegalHelpClaimDetailsViewField.VALUE_ROWS, details),
            buildRows(LegalHelpClaimDetailsViewField.TOTAL_ROWS, details));
      }
      case MEDIATION -> {
        var details = mediationClaimDetailsMapper.toMediationClaimDetails(claimResponse);
        yield new ClaimDetailView.Mediation(
            details,
            buildRows(MediationClaimDetailsViewField.VALUE_ROWS, details),
            buildRows(MediationClaimDetailsViewField.TOTAL_ROWS, details));
      }
    };
  }

  private <T> List<ClaimValueRow> buildRows(List<? extends ClaimViewField<T>> fields, T details) {
    return fields.stream()
        .map(
            field ->
                new ClaimValueRow(
                    field.label(messageSource), (ClaimFieldRow) field.getAccessor().apply(details)))
        .toList();
  }
}
