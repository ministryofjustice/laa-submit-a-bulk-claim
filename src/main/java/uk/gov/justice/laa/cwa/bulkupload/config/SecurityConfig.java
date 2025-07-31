package uk.gov.justice.laa.cwa.bulkupload.config;

import static org.springframework.security.config.Customizer.withDefaults;

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

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers("/webjars/**", "/assets/**", "/javascripts/**", "/stylesheets/**");
  }


  /**
   * UserDetailsService bean for in-memory user management. This method creates fake users for
   * testing purposes.
   *
   * @return the UserDetailsService instance
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository)
      throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/logged-out")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .csrf(Customizer.withDefaults())
        .oauth2Login(oauth2Login ->
            oauth2Login.loginPage("/oauth2/authorization/silas-identity"))
        .oauth2Client(withDefaults())
        .logout(
            logout ->
                logout
                    .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))

        );

    return http.build();
  }

  private LogoutSuccessHandler oidcLogoutSuccessHandler(
      ClientRegistrationRepository clientRegistrationRepository) {
    OidcClientInitiatedLogoutSuccessHandler successHandler =
        new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    successHandler.setPostLogoutRedirectUri("{baseUrl}/logged-out");
    return successHandler;
  }
}
