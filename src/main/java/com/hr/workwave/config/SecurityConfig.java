package com.hr.workwave.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final Logger THREAT_LOG = LoggerFactory.getLogger("SECURITY_THREAT");

    private static final String[] AUTH_WHITELIST = {
            // Swagger / OpenAPI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/v2/api-docs",
            "/api-docs/**",
            "/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
            // App
            "/api/v1/user/info",
            //GRAPHIQL
            "/graphiql**",
            "/graphql**",
            // Error forwarding
            "/error"
    };

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.proxy.host:}")
    private String proxyHost;

    @Value("${app.proxy.port:0}")
    private int proxyPort;

    @Value("${app.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.jwt.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${app.jwt.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Bean
    public ReactiveJwtDecoder customDecoder() {
        HttpClient httpClient = HttpClient.create();

        if (!proxyHost.isEmpty() && proxyPort > 0) {
            httpClient = httpClient.proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.HTTP)
                    .host(proxyHost)
                    .port(proxyPort));
        }

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(this.jwkSetUri)
                .webClient(WebClient.builder().clientConnector(connector).build())
                .build();

        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuerUri);
        decoder.setJwtValidator(validator);

        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            SecurityAuditFilter securityAuditFilter,
            JwtDecoder jwtDecoder
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .addFilterBefore(securityAuditFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint((request, response, authException) -> {
                            THREAT_LOG.warn("AUTH_FAILED ip={} uri={} reason=\"{}\"",
                                    request.getRemoteAddr(),
                                    request.getRequestURI(),
                                    authException.getMessage());
                            response.sendError(
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    authException.getMessage()
                            );
                        })
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        if (!proxyHost.isEmpty() && proxyPort > 0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
        }

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        return NimbusJwtDecoder
                .withJwkSetUri(this.jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(UserDetailsService userDetailsService) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("upn");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String username = jwt.getClaimAsString("upn");
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                return new ArrayList<>(userDetails.getAuthorities());
            } catch (UsernameNotFoundException e) {
                log.warn("User not found in DB: {} — continuing without user details.", username);
                return Collections.emptyList();
            }
        });

        return converter;
    }
}
