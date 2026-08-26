package uk.gov.justice.laa.payments.submit.config.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiProperties {

  private final String url;
  private final String accessToken;
}
