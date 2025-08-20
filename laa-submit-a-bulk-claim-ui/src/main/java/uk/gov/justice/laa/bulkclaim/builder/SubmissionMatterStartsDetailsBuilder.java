package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionMatterStartsMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

@Service
@RequiredArgsConstructor
public class SubmissionMatterStartsDetailsBuilder {

  private final DataClaimsRestService dataClaimsRestService;
  private final SubmissionMatterStartsMapper mapper;

  public SubmissionMatterStartsDetails build(GetSubmission200Response response) {
    List<SubmissionMatterStartsRow> list =
        response.getMatterStarts().stream()
            .map(
                x ->
                    dataClaimsRestService.getSubmissionMatterStarts(
                        response.getSubmission().getSubmissionId(), x))
            .map(x -> x.block())
            .map(x -> mapper.toSubmissionMatterTypesRow(x))
            .toList();
    return new SubmissionMatterStartsDetails(list);
  }
}
