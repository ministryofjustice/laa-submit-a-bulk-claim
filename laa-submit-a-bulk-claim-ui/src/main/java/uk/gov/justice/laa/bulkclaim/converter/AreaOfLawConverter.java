package uk.gov.justice.laa.bulkclaim.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Component
public class AreaOfLawConverter implements Converter<String, AreaOfLaw> {

  @Override
  public AreaOfLaw convert(String source) {
    if (source == null || source.isEmpty()) {
      return null;
    }
    return AreaOfLaw.fromValue(source.toUpperCase());
  }
}
