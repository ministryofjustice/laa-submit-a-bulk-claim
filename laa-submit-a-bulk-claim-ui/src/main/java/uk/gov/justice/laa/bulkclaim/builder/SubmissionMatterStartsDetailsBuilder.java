package uk.gov.justice.laa.bulkclaim.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Builder class responsible for creating a {@link List} of {@link SubmissionMatterStartsRow}.
 *
 * @author Jamie Briggs
 */
@Service
public class SubmissionMatterStartsDetailsBuilder {

  public static final String NEW_MATTER_STARTS_LABEL = "New matter starts";
  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionMatterStartsMapper mapper;

  public SubmissionMatterStartsDetailsBuilder(
      DataClaimsRestClient dataClaimsRestClient, SubmissionMatterStartsMapper mapper) {
    this.dataClaimsRestClient = dataClaimsRestClient;
    this.mapper = mapper;
  }

  /**
   * Builds a list of SubmissionMatterStartsRow objects.
   *
   * @param response The source submission response.
   * @return The built list of {@link SubmissionMatterStartsRow} objects.
   */
  public List<SubmissionMatterStartsRow> build(final SubmissionResponse response) {
    List<SubmissionMatterStartsRow> result = new ArrayList<>();

    Mono<MatterStartResultSet> allMatterStartsForSubmission =
        dataClaimsRestClient.getAllMatterStartsForSubmission(response.getSubmissionId());

    Assert.notNull(response.getAreaOfLaw(), "Area of Law is null");

    if (response.getAreaOfLaw().toLowerCase().contains("legal")) {
      allMatterStartsForSubmission.subscribe(
          matterStarterResultSet -> addLegalHelpMatterStarts(matterStarterResultSet, result));
    } else if (response.getAreaOfLaw().toLowerCase().contains("mediation")) {
      allMatterStartsForSubmission.subscribe(
          matterStarterResultSet -> addMediationMatterStarts(matterStarterResultSet, result));
    }

    return result;
  }

  private boolean addLegalHelpMatterStarts(
      MatterStartResultSet matterStarterResultSet, List<SubmissionMatterStartsRow> result) {
    return result.addAll(
        matterStarterResultSet.getMatterStarts().stream()
            // Filter by only category code matter starts
            .filter(x -> Objects.nonNull(x.getCategoryCode()))
            .map(mapper::toSubmissionMatterTypesRow)
            .toList());
  }

  private void addMediationMatterStarts(
      MatterStartResultSet matterStarterResultSet, List<SubmissionMatterStartsRow> result) {
    long totalMatterStartsMediationTypes =
        matterStarterResultSet.getMatterStarts().stream()
            // Filter by only category code matter starts
            .filter(x -> Objects.nonNull(x.getMediationType()))
            .mapToLong(MatterStartGet::getNumberOfMatterStarts)
            .sum();
    result.add(
        new SubmissionMatterStartsRow(NEW_MATTER_STARTS_LABEL, totalMatterStartsMediationTypes));
  }
}
