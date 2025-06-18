package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for the Bulk Upload application.
 * This configuration sets up basic authentication with an in-memory user store.
 */
@Profile("!test") // disable security for test profile
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * UserDetailsService bean for in-memory user management.
     * This method creates a user with username "ERNESTCOHEN" and password "password".
     *
     * @return the UserDetailsService instance
     */
    @Bean
    public UserDetailsService userDetailsService() {
        var user = User
                .withUsername("ERNESTCOHEN")
                .password("{noop}password") // {noop} means no password encoder
                .roles("USER")
                .build();

        var user2 = User
                .withUsername("JANEDOE")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user, user2);
    }

    /**
     * Security filter chain for the application.
     * This method configures HTTP security to require authentication for all requests
     * and uses basic authentication.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());
        return http.build();
    }
}