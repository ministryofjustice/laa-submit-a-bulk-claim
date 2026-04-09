package uk.gov.justice.laa.bulkclaim.accessibility.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Accessibility-test security configuration.
 *
 * <p>The accessibility suite validates rendered pages, not authentication flows. This configuration
 * injects a deterministic OIDC principal into every request so templates depending on SILAS claims
 * (for example firm name/code and office accounts) can render consistently without external IdP
 * calls.
 */
@Configuration
public class TestOidcUserConfig {

  /**
   * Registers a permissive filter chain for accessibility tests only.
   *
   * <p>Requests are allowed through and a synthetic authenticated user is populated by a custom
   * filter before {@link AnonymousAuthenticationFilter} runs.
   */
  @Bean
  @Order(0)
  SecurityFilterChain accessibilitySecurityFilterChain(
      HttpSecurity http, OncePerRequestFilter accessibilityOidcUserFilter) throws Exception {
    http.securityMatcher("/**")
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .oauth2Login(oauth2 -> oauth2.disable())
        .oauth2Client(oauth2 -> oauth2.disable())
        .addFilterBefore(accessibilityOidcUserFilter, AnonymousAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Injects a fixed OIDC user into the {@code SecurityContext} for each request.
   *
   * <p>The filter also forces session creation so CSRF and session-backed MVC flows behave the same
   * way as normal app requests in browser-based tests.
   */
  @Bean
  OncePerRequestFilter accessibilityOidcUserFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(
          jakarta.servlet.http.HttpServletRequest request,
          jakarta.servlet.http.HttpServletResponse response,
          jakarta.servlet.FilterChain filterChain)
          throws java.io.IOException, jakarta.servlet.ServletException {
        request.getSession(true);
        OidcUser user = buildUser();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
          filterChain.doFilter(request, response);
        } finally {
          SecurityContextHolder.clearContext();
        }
      }
    };
  }

  /** Builds the deterministic OIDC principal used by accessibility browser tests. */
  private static OidcUser buildUser() {
    Map<String, Object> mockOidcSabc =
        Map.of(
            "sub", "accessibility-user",
            "preferred_username", "accessibility-user@justice.gov.uk",
            "FIRM_NAME", "Accessibility Test Firm",
            "LAA_ACCOUNTS", List.of("0P322F", "2Q779P"),
            "FIRM_CODE", "1234");

    OidcIdToken idToken =
        new OidcIdToken(
            "test-id-token", Instant.now(), Instant.now().plusSeconds(3600), mockOidcSabc);
    return new DefaultOidcUser(List.of(), idToken, "preferred_username");
  }
}
