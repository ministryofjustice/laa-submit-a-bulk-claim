package uk.gov.justice.laa.payments.submit.viewmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thymeleaf.expression.Messages;

@AllArgsConstructor
@Getter
public class ThymeleafLiteralString extends ThymeleafString {

  private final String value;

  @Override
  public String resolve(Messages messages) {
    return value;
  }
}
