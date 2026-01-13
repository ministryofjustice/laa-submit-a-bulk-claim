package uk.gov.justice.laa.bulkclaim.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * Security configuration for the Bulk Upload application. This configuration sets up basic
 * authentication with an in-memory user store.
 */
@Profile("!test") // disable security for test profile
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Configures web security to ignore requests for static resources. This allows assets like
   * webjars, stylesheets, and JavaScripts to be served without authentication.
   *
   * @return a WebSecurityCustomizer that ignores specified static resource paths
   */
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web ->
        web.ignoring()
            .requestMatchers(
                "/webjars/**",
                "/assets/**",
                "/javascripts/**",
                "/stylesheets/**",
                "/actuator/prometheus",
                "/actuator/health",
                "/actuator/info");
  }

  /**
   * UserDetailsService bean for in-memory user management. This method creates fake users for
   * testing purposes.
   *
   * @return the UserDetailsService instance
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ClientRegistrationRepository clientRegistrationRepository,
      @Value("${app.csp}") String contentSecurityPolicy) {
    http
        .authorizeHttpRequests(
            authz -> //
                authz
                    .requestMatchers("/logged-out")
                    .permitAll()
                    .anyRequest() //
                    .authenticated())
        .csrf(Customizer.withDefaults())
        .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer
            .contentSecurityPolicy(csp -> csp.policyDirectives(contentSecurityPolicy)))
        .oauth2Login(
            oauth2Login -> //
                oauth2Login.loginPage("/oauth2/authorization/silas-identity"))
        .oauth2Client(withDefaults())
        .logout(
            logout ->
                logout.logoutSuccessHandler(
                    oidcLogoutSuccessHandler(clientRegistrationRepository)));
    return http.build();
  }

  private LogoutSuccessHandler oidcLogoutSuccessHandler(
      ClientRegistrationRepository clientRegistrationRepository) {
    OidcClientInitiatedLogoutSuccessHandler successHandler =
        new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    successHandler.setPostLogoutRedirectUri("{baseUrl}/logged-out");
    return successHandler;
  }

  private String generateNonce() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] nonceBytes = new byte[16];
    secureRandom.nextBytes(nonceBytes);
    return Base64.getEncoder().encodeToString(nonceBytes);
  }
}
