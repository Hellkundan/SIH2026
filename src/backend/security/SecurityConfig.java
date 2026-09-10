package backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .cors(cors -> { })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tenders/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/tenders/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tenders/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tenders/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/bidders/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/bidders/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/bidders/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bidders/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/documents/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/documents").hasRole("BIDDER")
                        .requestMatchers(HttpMethod.POST, "/documents/*/processing")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/documents/*/processed")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/documents/*/failed")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/documents/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tender-bids/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/tender-bids").hasRole("BIDDER")
                        .requestMatchers(HttpMethod.POST, "/tender-bids/*/submit").hasRole("BIDDER")
                        .requestMatchers(HttpMethod.POST, "/tender-bids/*/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tender-bids/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tender-requirements/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/tender-requirements/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tender-requirements/**")
                        .hasAnyRole("PROCUREMENT_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tender-requirements/**")
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