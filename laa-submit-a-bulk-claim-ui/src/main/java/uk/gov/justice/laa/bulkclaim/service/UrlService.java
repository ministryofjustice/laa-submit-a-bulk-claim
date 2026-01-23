package uk.gov.justice.laa.bulkclaim.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class for handling operations related to URL management. This class provides the
 * canonical base URL for the application.
 *
 * @author Jamie Briggs
 */
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
