package uk.gov.justice.laa.payments.submit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionMatterStartsRow;

@Mapper(componentModel = "spring")
public interface SubmissionMatterStartsMapper {

  @Mapping(target = "description", source = "categoryCode")
  SubmissionMatterStartsRow toSubmissionMatterTypesRow(MatterStartGet matterStartsGet);
}
