package uk.gov.justice.laa.bulkclaim.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class UrlService {

  private final String canonicalBaseUrl;

  public UrlService(@Value("${app.canonical-base-url}") String canonicalBaseUrl) {
    this.canonicalBaseUrl = canonicalBaseUrl;
  }

  public String buildAbsoluteUrl(String path) {
    String cleanPath = path.startsWith("/") ? path : "/" + path;
    return canonicalBaseUrl + cleanPath;
  }
}
