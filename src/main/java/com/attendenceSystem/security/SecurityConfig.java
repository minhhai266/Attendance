package com.attendenceSystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

// import com.attendenceSystem.constant.Routes;
// import com.attendenceSystem.entity.enums.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // private final ActiveUserFilter activeUserFilter;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/dashboard/**", "/attendance/**", "/attendance", "/attendanceCheck",
                                "/attendanceHistory", "/attendanceLists")
                        .permitAll()
                        .requestMatchers("/api/attendance/**").permitAll()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // If it's an API request or AJAX request, return 401
                            if (request.getRequestURI().startsWith("/api/") ||
                                    "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                // For normal page requests, redirect to home page
                                response.sendRedirect(request.getContextPath() + "/");
                            }
                        }))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép frontend Flutter web (5000) truy cập
        configuration.setAllowedOrigins(List.of("http://localhost:5000", "http://127.0.0.1:5000"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration
                .setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-CSRF-Token"));

        // Cực kỳ quan trọng để Flutter có thể lưu Session/Cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng luật CORS này cho tất cả các endpoint (/**)
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    // Chỉ dùng để test admin
    // @Bean
    // public UserDetailsService userDetailsService(
    // PasswordEncoder passwordEncoder) {

    // UserDetails admin = User.builder()
    // .username("admin")
    // .password(passwordEncoder.encode("123456"))
    // .roles(Role.ADMIN.name())
    // .build();

    // return new InMemoryUserDetailsManager(admin);
    // }
}
