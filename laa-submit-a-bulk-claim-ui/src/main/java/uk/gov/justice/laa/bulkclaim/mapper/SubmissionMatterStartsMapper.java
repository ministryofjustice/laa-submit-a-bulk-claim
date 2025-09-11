package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;

/**
 * Maps between {@link MatterStartGet} and {@link SubmissionMatterStartsRow}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface SubmissionMatterStartsMapper {

  /**
   * Maps a {@link MatterStartGet} to a {@link SubmissionMatterStartsRow}.
   *
   * @param matterStartsGet The source matter starts fields object.
   * @return The mapped {@link SubmissionMatterStartsRow}.
   */
  @Mapping(target = "description", source = "categoryCode")
  SubmissionMatterStartsRow toSubmissionMatterTypesRow(MatterStartGet matterStartsGet);
}
