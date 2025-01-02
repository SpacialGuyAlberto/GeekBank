package com.geekbank.bank.config;

import com.geekbank.bank.components.JwtAuthenticationFilter;
import com.geekbank.bank.filters.JwtRequestFilter;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.UserDetailsServiceImpl;
import com.geekbank.bank.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Value("${DOMAIN_ORIGIN_URL}")
    private String frontendUrl;

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    public SecurityConfig(JwtTokenUtil jwtTokenUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        // Rutas públicas (permitAll)
                        .requestMatchers(
                                "/api/ws/**",
                                "/api/auth/**",
                                "/api/transactions/{transactionId}",
                                "/api/transactions/cancel/**",
                                "/api/auth/registerUser", "/api/auth/check-auth",
                                "/api/auth/registerUserByAdmin", "/api/auth/activate",
                                "/api/auth/validate-password", "/api/auth/login",
                                "/api/auth/google-login", "/api/auth/logout",
                                "/api/auth/reset-password", "/api/accounts/**",
                                "/api/accounts/apply-balance/", "/api/home",
                                "/api/gift-cards/**", "/api/kinguin-gift-cards/**",
                                "/api/users/**", "/api/public/**", "/api/cart",
                                "/api/cart/**", "/api/telegram/**", "/api/kinguin/**",
                                "/api/users/user-details", "/api/users/${userId}",
                                "/api/orders", "/api/orders/**", "/api/highlights/**",
                                "/api/highlights", "/api/users/update-user-details",
                                "/api/users/**", "/api/transactions", "/api/transactions/**",
                                "/api/transactions/cancel/**", "/api/wish-list",
                                "/api/wish-list/**", "/api/wish-list/${wishedItemId}",
                                "/api/feedbacks/**", "/api/recommendations/**",
                                "/api/recommendations/user/${userId}", "/api/sync/**",
                                "/api/freefire/**", "/api/freefire/products",
                                "/api/currency", "/api/recommendations/content-based/**",
                                "/api/manual-orders/**", "/api/main-screen-gift-cards/**",
                                "api/transactions/verifyPayment",
                                "/api/activation-details/**",
                                "api/transactions/verify-unmatched-payment",
                                "/api/paypal/**", "/api/auth/check-auth", "/api/visits/**", "/api/metrics/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/activation-details").hasAnyRole("ADMIN", "SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/activation-details/**").hasAnyRole("ADMIN", "SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/activation-details/**").hasAnyRole("ADMIN", "SUPPORT", "CUSTOMER", "GUEST")
                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // ***** Si no quieres redirecciones a login, COMENTA o QUITA oauth2Login() *****
                // .oauth2Login(oauth2Login -> oauth2Login
                //     .defaultSuccessUrl("/api/home", true)
                //     .userInfoEndpoint(userInfoEndpoint ->
                //         userInfoEndpoint.oidcUserService(oidcUserService()))
                // )

                // Deshabilitar CSRF (opcional en aplicaciones REST).
                .csrf(csrf -> csrf.disable())


                // Configura CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Devuelve 401 en lugar de 302 cuando no haya auth
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    authException.getMessage()
                            );
                        })
                )

                // Sessions stateless (JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Inserta el filtro que valida JWT antes del UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Filtro que maneja el JWT
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);
    }

    // Configuración para login con Google (si quisieras usar oauth2Login)
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(googleClientRegistration());
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("445500636748-2nuqarr3morlrul9bdadefcogo7rffcn.apps.googleusercontent.com")
                .clientSecret("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")
                .scope("openid", "profile", "email")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }

    // Configuración CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("https://astralisbank.com", "http://localhost:4200"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.addAllowedOrigin(frontendUrl);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a todas las rutas
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }

    // Manager de autenticación
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    // Carga el servicio de usuarios
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(userRepository);
    }

    // Encoder para las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // Decoder para tokens JWT de Google (si quisieras validar ID token, etc.)
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();
    }
}
