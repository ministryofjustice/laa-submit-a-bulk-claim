package uk.gov.justice.laa.bulkclaim.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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
@Slf4j
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

    List<MatterStartGet> matterStarts =
        dataClaimsRestClient
            .getAllMatterStartsForSubmission(response.getSubmissionId())
            .blockOptional()
            .map(MatterStartResultSet::getMatterStarts)
            .orElse(Collections.emptyList());

    Assert.notNull(response.getAreaOfLaw(), "Area of Law is null");

    switch (response.getAreaOfLaw()) {
      case LEGAL_HELP -> addLegalHelpMatterStarts(matterStarts, result);
      case MEDIATION -> addMediationMatterStarts(matterStarts, result);
      default ->
          log.debug("No extra content required for Area of Law: {}", response.getAreaOfLaw());
    }

    return result;
  }

  private void addLegalHelpMatterStarts(
      List<MatterStartGet> matterStarts, List<SubmissionMatterStartsRow> result) {
    result.addAll(
        matterStarts.stream()
            // Filter by only category code matter starts
            .filter(x -> Objects.nonNull(x.getCategoryCode()))
            .map(mapper::toSubmissionMatterTypesRow)
            .toList());
  }

  private void addMediationMatterStarts(
      List<MatterStartGet> matterStarts, List<SubmissionMatterStartsRow> result) {
    long totalMatterStartsMediationTypes =
        matterStarts.stream()
            // Filter by only category code matter starts
            .filter(x -> Objects.nonNull(x.getMediationType()))
            .mapToLong(MatterStartGet::getNumberOfMatterStarts)
            .sum();
    if (totalMatterStartsMediationTypes > 0) {
      result.add(
          new SubmissionMatterStartsRow(NEW_MATTER_STARTS_LABEL, totalMatterStartsMediationTypes));
    }
  }
}
