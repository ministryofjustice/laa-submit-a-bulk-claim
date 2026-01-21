package uk.gov.justice.laa.bulkclaim.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter to validate the Host header against allowed hosts.
 *
 * @author Jamie Briggs
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HostValidationFilter extends OncePerRequestFilter {

  private final SecurityProperties securityProperties;

  /** Filters request -> rejects invalid host or forward host -> continues valid requests. */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String host = request.getHeader("Host");
    String forwardedHost = request.getHeader("X-Forwarded-Host");

    // Rejects request when host or forwarded host invalid
    if (isInvalid(host)) {
      log.error("Invalid Host header: {}", host);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Host header");
      return;
    } else if (forwardedHost != null && isInvalid(forwardedHost)) {
      log.error("Invalid X-Forwarded-Host header: {}", forwardedHost);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Forwarded-Host header");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isInvalid(String host) {
    if (host == null) {
      return true;
    }
    // Strip port if present for comparison
    String hostname = host.split(":")[0];
    return !securityProperties.getAllowedHosts().contains(hostname);
  }
}
