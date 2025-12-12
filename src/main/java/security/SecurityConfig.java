package security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import security.JwtAuthFilter;
import security.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwt;

    public SecurityConfig(JwtUtil jwt) {
        this.jwt = jwt;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwt);

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(
                org.springframework.security.config.http.SessionCreationPolicy.STATELESS
        ));
        http.addFilterBefore(filter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/api/public").permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
}