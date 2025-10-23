package uk.gov.justice.laa.bulkclaim.builder;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Builder class responsible for creating a {@link List} of {@link SubmissionMatterStartsRow}.
 *
 * @author Jamie Briggs
 */
@Service
@RequiredArgsConstructor
public class SubmissionMatterStartsDetailsBuilder {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionMatterStartsMapper mapper;

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

    allMatterStartsForSubmission.subscribe(
        matterStarterResultSet ->
            result.addAll(
                matterStarterResultSet.getMatterStarts().stream()
                    .map(mapper::toSubmissionMatterTypesRow)
                    .toList()));

    return result;
  }
}
