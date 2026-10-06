package br.com.delta.delta_api_postgres.modules.device.config;

import br.com.delta.delta_api_postgres.modules.auth.security.SecurityErrorHandler;
import br.com.delta.delta_api_postgres.modules.device.security.MongoServiceAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(name = "mongoServiceAuth",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP, scheme = "bearer")
public class DeviceIntegrationSecurityConfig {
    @Bean
    @Order(1)
    SecurityFilterChain deviceIntegrationFilterChain(HttpSecurity http, DeviceIntegrationProperties properties,
                                                    SecurityErrorHandler errors) throws Exception {
        return http.securityMatcher("/delta/internal/device-auth/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(logout -> logout.disable())
                // Scoped to this stateless service-to-service chain; user security remains independent.
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(errors).accessDeniedHandler(errors))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/delta/internal/device-auth/validate")
                        .hasAuthority(MongoServiceAuthenticationFilter.VALIDATE_AUTHORITY)
                        .anyRequest().denyAll())
                .addFilterBefore(new MongoServiceAuthenticationFilter(properties, errors), AnonymousAuthenticationFilter.class)
                .build();
    }
}
