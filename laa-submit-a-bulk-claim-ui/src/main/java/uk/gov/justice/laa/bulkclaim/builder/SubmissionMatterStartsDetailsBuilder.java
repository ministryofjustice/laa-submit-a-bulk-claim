package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Builder class responsible for creating instances of SubmissionMatterStartsDetails.
 *
 * @author Jamie Briggs
 */
@Service
@RequiredArgsConstructor
public class SubmissionMatterStartsDetailsBuilder {

  private final DataClaimsRestService dataClaimsRestService;
  private final SubmissionMatterStartsMapper mapper;

  /**
   * Builds a {@link SubmissionMatterStartsDetails} object.
   *
   * @param response The source submission response.
   * @return The built {@link SubmissionMatterStartsDetails} object.
   */
  public SubmissionMatterStartsDetails build(GetSubmission200Response response) {
    List<SubmissionMatterStartsRow> list =
        response.getMatterStarts().stream()
            .map(
                x ->
                    dataClaimsRestService.getSubmissionMatterStarts(
                        response.getSubmission().getSubmissionId(), x))
            .map(Mono::block)
            .map(mapper::toSubmissionMatterTypesRow)
            .toList();
    return new SubmissionMatterStartsDetails(list);
  }
}
