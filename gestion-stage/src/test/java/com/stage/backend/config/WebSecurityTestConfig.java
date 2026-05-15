package com.stage.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Configuration de sécurité pour les tests @WebMvcTest.
 *
 * CORRECTIFS :
 * 1. CSRF désactivé → les requêtes sans token ne sont plus bloquées par CsrfFilter
 * 2. Session STATELESS → pas de session HTTP créée
 * 3. /api/auth/** → permitAll() comme en production
 * 4. /api/statistiques/** → hasRole("ADMIN") pour tester les 403
 * 5. authenticationEntryPoint(HttpStatusEntryPoint(UNAUTHORIZED)) →
 *    les requêtes sans authentification reçoivent 401 (pas 403)
 *    C'est le comportement attendu par les tests signin_badCredentials_returns401
 *    et getStats_unauthenticated_returns401.
 */
@TestConfiguration
public class WebSecurityTestConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/statistiques/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // CORRECTIF CLÉ : retourner 401 (pas 403) quand non authentifié
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );
        return http.build();
    }
}