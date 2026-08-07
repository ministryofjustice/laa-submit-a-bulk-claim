package uk.gov.justice.laa.bulkclaim.service.claimdetail;

import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;

public sealed interface ClaimDetailView {

  String template();

  List<ClaimFieldRow> valueRows();

  record CrimeLower(CrimeLowerClaimDetails details, List<ClaimFieldRow> valueRows)
      implements ClaimDetailView {

    @Override
    public String template() {
      return "pages/view-claim-detail-crime-lower";
    }
  }

  record LegalHelp(LegalHelpClaimDetails details, List<ClaimFieldRow> valueRows)
      implements ClaimDetailView {

    @Override
    public String template() {
      return "pages/view-claim-detail-legal-help";
    }
  }

  record Mediation(MediationClaimDetails details, List<ClaimFieldRow> valueRows)
      implements ClaimDetailView {

    @Override
    public String template() {
      return "pages/view-claim-detail-mediation";
    }
  }
}
