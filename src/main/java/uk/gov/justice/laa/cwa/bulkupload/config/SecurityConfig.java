package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Bulk Upload application.
 * This configuration sets up basic authentication with an in-memory user store.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * UserDetailsService bean for in-memory user management.
     * This method creates fake users for testing purposes.
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

        var user4 = User
                .withUsername("DT_SCRIPT_USER4")
                .password("{noop}password")
                .roles("USER")
                .build();

        var user6 = User
                .withUsername("DT_SCRIPT_USER6")
                .password("{noop}password")
                .roles("USER")
                .build();

        var user14 = User
                .withUsername("DT_SCRIPT_USER14")
                .password("{noop}password")
                .roles("USER")
                .build();

        var user19 = User
                .withUsername("DT_SCRIPT_USER19")
                .password("{noop}password")
                .roles("USER")
                .build();

        var user22 = User
                .withUsername("DT_SCRIPT_USER22")
                .password("{noop}password")
                .roles("USER")
                .build();

        var user23 = User
                .withUsername("DT_SCRIPT_USER23")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user, user4, user6, user14, user19, user22, user23);
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
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/assets/**", "/javascripts/**", "/stylesheets/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.logoutSuccessUrl("/"));

        return http.build();

    }
}