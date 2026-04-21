package org.example.spring_hw.config;

import org.example.spring_hw.security.JwtAuthFilter;
import org.example.spring_hw.security.PepperPasswordEncoder;
import org.example.spring_hw.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
                                          JwtAuthFilter jwtAuthFilter,
                                          RestAuthenticationEntryPoint entryPoint) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            .requestMatchers("/external/**", "/actuator/health", "/actuator/metrics", "/actuator/metrics/**", "/error").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/profile").hasRole("USER")
            .requestMatchers(HttpMethod.GET, "/api/v1/docs").hasAuthority("READ_PRIVILEGE")
            .requestMatchers("/api/v1/**").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    return new InMemoryUserDetailsManager(
        User.withUsername("user")
            .password(passwordEncoder.encode("password"))
            .authorities("ROLE_USER")
            .build(),
        User.withUsername("reader")
            .password(passwordEncoder.encode("password"))
            .authorities("ROLE_USER", "READ_PRIVILEGE")
            .build()
    );
  }

  @Bean
  PasswordEncoder passwordEncoder(@Value("${app.security.password-pepper}") String pepper) {
    return new PepperPasswordEncoder(new BCryptPasswordEncoder(), pepper);
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }
}
