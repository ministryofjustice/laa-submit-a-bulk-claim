package uk.gov.justice.laa.payments.submit.viewmodels;

import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.thymeleaf.expression.Messages;

@EqualsAndHashCode(callSuper = false)
@Getter
public class ThymeleafMessage extends ThymeleafString {

  private final String key;
  private final Object[] params;

  public ThymeleafMessage(String key, Object... params) {
    this.key = key;
    this.params = params == null ? new Object[0] : params.clone();
  }

  @Override
  public String resolve(Messages messages) {
    Object[] resolvedParams =
        Arrays.stream(params).map(param -> resolveParam(param, messages)).toArray();
    return messages.msgWithParams(key, resolvedParams);
  }

  private String resolveParam(Object value, Messages messages) {
    return switch (value) {
      case null -> "";
      case ThymeleafMessage tm -> tm.resolve(messages);
      case String s -> s;
      default -> String.valueOf(value);
    };
  }
}
