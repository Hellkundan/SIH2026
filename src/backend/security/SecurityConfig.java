package backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService
    ) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("GET", "/tenders/**").authenticated()
                        .requestMatchers("POST", "/tenders/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("PUT", "/tenders/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("DELETE", "/tenders/**")
                        .hasRole("ADMIN")
                        .requestMatchers("GET", "/bidders/**").authenticated()
                        .requestMatchers("POST", "/bidders/**")
                        .hasRole("ADMIN")
                        .requestMatchers("PUT", "/bidders/**")
                        .hasRole("ADMIN")
                        .requestMatchers("DELETE", "/bidders/**")
                        .hasRole("ADMIN")
                        .requestMatchers("GET", "/documents/**").authenticated()
                        .requestMatchers("POST", "/documents").hasRole("BIDDER")
                        .requestMatchers("POST", "/documents/*/processing")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("POST", "/documents/*/processed")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("POST", "/documents/*/failed")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("DELETE", "/documents/**")
                        .hasRole("ADMIN")
                        .requestMatchers("GET", "/tender-bids/**").authenticated()
                        .requestMatchers("POST", "/tender-bids").hasRole("BIDDER")
                        .requestMatchers("POST", "/tender-bids/*/submit").hasRole("BIDDER")
                        .requestMatchers("POST", "/tender-bids/*/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("DELETE", "/tender-bids/**")
                        .hasRole("ADMIN")
                        .requestMatchers("GET", "/tender-requirements/**").authenticated()
                        .requestMatchers("POST", "/tender-requirements/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("PUT", "/tender-requirements/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers("DELETE", "/tender-requirements/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}