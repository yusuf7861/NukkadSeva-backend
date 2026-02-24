package com.nukkadseva.nukkadsevabackend.config;

import com.nukkadseva.nukkadsevabackend.security.CustomAccessDeniedHandler;
import com.nukkadseva.nukkadsevabackend.security.CustomAuthenticationEntryPoint;
import com.nukkadseva.nukkadsevabackend.security.JwtAuthenticationFilter;
import com.nukkadseva.nukkadsevabackend.util.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {

        private final AppUserDetailsService userDetailsService;

        @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
        private String baseUrl;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter, CustomAuthenticationEntryPoint entryPoint,
                        CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(
                                                authorize -> authorize
                                                                .requestMatchers(
                                                                                // public endpoints
                                                                                "/api/login",
                                                                                "/api/customer/register",
                                                                                "/api/provider/register",
                                                                                "/api/public/**",
                                                                                "/api/services/search",
                                                                                "/api/logout",
                                                                                "/api/forgot-password",
                                                                                "/api/reset-password",
                                                                                "/api/provider/verify-email",
                                                                                "/api/verify-email",
                                                                                "/ws/**",
                                                                                // Swagger endpoints
                                                                                "/v3/api-docs/**",
                                                                                "/swagger-ui/**",
                                                                                "/swagger-ui.html")
                                                                .permitAll()
                                                                .anyRequest().authenticated())
                                .exceptionHandling(
                                                ex -> ex.authenticationEntryPoint(entryPoint)
                                                                .accessDeniedHandler(accessDeniedHandler))
                                .sessionManagement(
                                                session -> session
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(
                                                jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsFilter corsFilter() {
                return new CorsFilter(corsConfigurationSource());
        }

        private UrlBasedCorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                                "http://localhost:5174",
                                "http://localhost:5173",
                                "http://localhost:9002",
                                "http://localhost:3000",
                                baseUrl,
                                baseUrl.replace(":8080", ":3000"), // In case frontend is hosted on the same IP but port
                                                                   // 3000
                                "https://nukkad-seva.vercel.app/"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cookie"));
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
                authenticationProvider.setUserDetailsService(userDetailsService);
                authenticationProvider.setPasswordEncoder(passwordEncoder());
                return new ProviderManager(authenticationProvider);
        }
}
