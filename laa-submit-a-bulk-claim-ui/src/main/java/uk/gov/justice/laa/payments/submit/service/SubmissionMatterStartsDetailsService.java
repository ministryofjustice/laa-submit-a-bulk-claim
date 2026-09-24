package uk.gov.justice.laa.payments.submit.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.payments.submit.mapper.SubmissionMatterStartsMapper;

@Slf4j
@Service
public class SubmissionMatterStartsDetailsService {

  public static final String NEW_MATTER_STARTS_LABEL = "New matter starts";
  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionMatterStartsMapper mapper;

  public SubmissionMatterStartsDetailsService(
      DataClaimsRestClient dataClaimsRestClient, SubmissionMatterStartsMapper mapper) {
    this.dataClaimsRestClient = dataClaimsRestClient;
    this.mapper = mapper;
  }

  public List<SubmissionMatterStartsRow> getAll(SubmissionResponse response) {
    if (response.getAreaOfLaw() == AreaOfLaw.CRIME_LOWER) {
      log.debug("No extra content required for Area of Law: {}", response.getAreaOfLaw());
      return List.of();
    }

    List<MatterStartGet> matterStarts =
        dataClaimsRestClient
            .getAllMatterStartsForSubmission(response.getSubmissionId())
            .blockOptional()
            .map(MatterStartResultSet::getMatterStarts)
            .orElse(Collections.emptyList());

    Assert.notNull(response.getAreaOfLaw(), "Area of Law is null");

    return switch (response.getAreaOfLaw()) {
      case LEGAL_HELP -> getLegalHelpMatterStarts(matterStarts);
      case MEDIATION -> getMediationMatterStarts(matterStarts);
      default -> {
        log.debug("No extra content required for Area of Law: {}", response.getAreaOfLaw());
        yield List.of();
      }
    };
  }

  private List<SubmissionMatterStartsRow> getLegalHelpMatterStarts(
      List<MatterStartGet> matterStarts) {
    return matterStarts.stream()
        // Filter by only category code matter starts
        .filter(x -> Objects.nonNull(x.getCategoryCode()))
        .map(mapper::toSubmissionMatterTypesRow)
        .toList();
  }

  private List<SubmissionMatterStartsRow> getMediationMatterStarts(
      List<MatterStartGet> matterStarts) {
    long totalMatterStartsMediationTypes =
        matterStarts.stream()
            // Filter by only category code matter starts
            .filter(x -> Objects.nonNull(x.getMediationType()))
            .mapToLong(MatterStartGet::getNumberOfMatterStarts)
            .sum();
    if (totalMatterStartsMediationTypes > 0) {
      return List.of(
          new SubmissionMatterStartsRow(NEW_MATTER_STARTS_LABEL, totalMatterStartsMediationTypes));
    }
    return List.of();
  }
}
