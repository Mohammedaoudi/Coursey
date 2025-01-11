package com.ensa.projet.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain actuatorFilterChain(ServerHttpSecurity http) {
        ServerWebExchangeMatcher actuatorMatcher = new PathPatternParserServerWebExchangeMatcher("/actuator/**");

        return http.securityMatcher(actuatorMatcher)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                    return config;
                }))
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                    config.setMaxAge(3600L);
                    return config;
                }))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers("/TRAINING-SERVICE/trainings/**").hasRole("USER")
                        .pathMatchers("/TRAINING-SERVICE/api/images/**").permitAll()
                        .pathMatchers("/*/api/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        ))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter jwtConverter = new ReactiveJwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            List<String> roles = Optional.ofNullable(realmAccess)
                    .map(access -> {
                        Object rolesObj = access.get("roles");
                        if (rolesObj instanceof List) {
                            return ((List<?>) rolesObj).stream()
                                    .filter(String.class::isInstance)
                                    .map(Object::toString)
                                    .toList();
                        }
                        return Collections.<String>emptyList();
                    })
                    .orElse(Collections.emptyList());

            return Flux.fromIterable(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collectList()
                    .flatMapMany(Flux::fromIterable);
        });
        return jwtConverter;
    }
}