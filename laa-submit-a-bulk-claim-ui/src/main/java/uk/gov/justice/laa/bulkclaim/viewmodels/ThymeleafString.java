package uk.gov.justice.laa.bulkclaim.viewmodels;

import org.thymeleaf.expression.Messages;

public abstract class ThymeleafString {

  public abstract String resolve(Messages messages);
}
