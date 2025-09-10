package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Builder class responsible for creating instances of SubmissionMatterStartsDetails.
 *
 * @author Jamie Briggs
 */
@Service
@RequiredArgsConstructor
public class SubmissionMatterStartsDetailsBuilder {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionMatterStartsMapper mapper;

  /**
   * Builds a {@link SubmissionMatterStartsDetails} object.
   *
   * @param response The source submission response.
   * @return The built {@link SubmissionMatterStartsDetails} object.
   */
  public SubmissionMatterStartsDetails build(SubmissionResponse response) {
    List<SubmissionMatterStartsRow> list =
        response.getMatterStarts().stream()
            .map(
                x -> dataClaimsRestClient.getSubmissionMatterStarts(response.getSubmissionId(), x))
            .map(Mono::block)
            .map(mapper::toSubmissionMatterTypesRow)
            .toList();

    Map<SubmissionMatterStartsRow, Long> rows =
        list.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting()));
    return new SubmissionMatterStartsDetails(rows);
  }
}
