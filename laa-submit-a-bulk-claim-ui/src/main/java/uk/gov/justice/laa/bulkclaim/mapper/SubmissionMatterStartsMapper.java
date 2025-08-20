package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.claims.model.MatterStartsFields;

@Mapper(componentModel = "spring")
public interface SubmissionMatterStartsMapper {

  @Mapping(target = "description", source = "categoryCode")
  SubmissionMatterStartsRow toSubmissionMatterTypesRow(MatterStartsFields matterStartsFields);
}
