package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.claims.model.MatterStartsGet;

/**
 * Maps between {@link MatterStartsGet} and {@link SubmissionMatterStartsRow}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface SubmissionMatterStartsMapper {

  /**
   * Maps a {@link MatterStartsGet} to a {@link SubmissionMatterStartsRow}.
   *
   * @param matterStartsGet The source matter starts fields object.
   * @return The mapped {@link SubmissionMatterStartsRow}.
   */
  @Mapping(target = "description", source = "categoryCode")
  SubmissionMatterStartsRow toSubmissionMatterTypesRow(MatterStartsGet matterStartsGet);
}
