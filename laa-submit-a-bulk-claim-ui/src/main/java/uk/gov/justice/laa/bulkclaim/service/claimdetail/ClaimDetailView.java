package uk.gov.justice.laa.bulkclaim.service.claimdetail;

import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimValueRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;

public sealed interface ClaimDetailView {

  String template();

  List<ClaimValueRow> valueRows();

  List<ClaimValueRow> totalRows();

  record CrimeLower(
      CrimeLowerClaimDetails details, List<ClaimValueRow> valueRows, List<ClaimValueRow> totalRows)
      implements ClaimDetailView {

    @Override
    public String template() {
      return "pages/view-claim-detail-crime-lower";
    }
  }

  record LegalHelp(
      LegalHelpClaimDetails details, List<ClaimValueRow> valueRows, List<ClaimValueRow> totalRows)
      implements ClaimDetailView {

    @Override
    public String template() {
      return "pages/view-claim-detail-legal-help";
    }
  }

  record Mediation(
      MediationClaimDetails details, List<ClaimValueRow> valueRows, List<ClaimValueRow> totalRows)
      implements ClaimDetailView {

    @Override
    public String template() {
      return "pages/view-claim-detail-mediation";
    }
  }
}
