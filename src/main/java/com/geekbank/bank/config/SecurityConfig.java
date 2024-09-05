package com.geekbank.bank.config;

import com.geekbank.bank.filters.JwtRequestFilter;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/registerUser", "/api/auth/activate", "/api/auth/validate-password","/api/auth/login", "/api/auth/google-login", "/api/auth/logout", "/api/auth/reset-password",
                                "/api/home", "/api/gift-cards/**", "/api/kinguin-gift-cards/**", "/api/users/**", "/api/public/**", "/api/cart", "/api/cart/**",
                                "/api/telegram/**", "/api/kinguin/**", "/api/users/user-details", "/api/users/${userId}", "/api/orders", "/api/orders/**",
                                "/api/highlights/**", "/api/highlights", "/api/users/update-user-details", "/api/users/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .defaultSuccessUrl("/api/home", true)
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.oidcUserService(oidcUserService()))
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
    }
}
