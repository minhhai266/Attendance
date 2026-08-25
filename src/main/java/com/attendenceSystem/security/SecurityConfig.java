package com.attendenceSystem.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.attendenceSystem.constant.Routes;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ActiveUserFilter activeUserFilter;
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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .invalidSessionUrl(Routes.Auth.ROOT + Routes.Auth.LOGIN))
                .addFilterBefore(activeUserFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // ------------------- Công khai (không cần đăng nhập) -------------------
                        .requestMatchers("/", "/home").permitAll()
                        .requestMatchers(Routes.Auth.ROOT + "/**", "/login/**").permitAll()
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/uploads/**", "/favicon.ico", "/error").permitAll()
                        // Các API đăng nhập / OTP
                        .requestMatchers(Routes.API + Routes.Auth.ROOT + "/**",
                                Routes.API + Routes.Otp.ROOT + "/**").permitAll()
                        // Kiosk điểm danh không cần đăng nhập
                        .requestMatchers(Routes.Attendance.ROOT + Routes.Attendance.CHECK).permitAll()
                        // Face ID đăng ký/điểm danh không cần đăng nhập
                        .requestMatchers(Routes.FaceId.ROOT + "/**",
                                Routes.API + Routes.FaceId.ROOT + "/**").permitAll()

                        // ------------------- ADMIN -------------------
                        .requestMatchers(Routes.Role.ADMIN + "/**",
                                Routes.API + Routes.Role.ADMIN + "/**",
                                Routes.System.ROOT + "/**",
                                Routes.API + Routes.System.ROOT + "/**",
                                Routes.Dashboard.ROOT + Routes.Role.ADMIN,
                                Routes.API + Routes.Dashboard.ROOT + Routes.Role.ADMIN)
                        .hasRole("ADMIN")

                        // ------------------- MANAGER -------------------
                        .requestMatchers(Routes.Role.MANAGER + "/**",
                                Routes.API + Routes.Role.MANAGER + "/**",
                                Routes.Dashboard.ROOT + Routes.Role.MANAGER,
                                Routes.API + Routes.Dashboard.ROOT + Routes.Role.MANAGER,
                                Routes.API + Routes.Attendance.ROOT + Routes.Role.MANAGER + "/**")
                        .hasRole("MANAGER")

                        // ------------------- Mọi người dùng đã đăng nhập -------------------
                        .requestMatchers(Routes.Dashboard.ROOT + "/**",
                                Routes.API + Routes.Dashboard.ROOT + "/**",
                                Routes.Attendance.ROOT + "/**",
                                Routes.API + Routes.Attendance.ROOT + "/**",
                                Routes.Schedule.ROOT + "/**",
                                Routes.Report.ROOT + "/**",
                                Routes.API + Routes.Report.ROOT + "/**",
                                Routes.User.ROOT + "/**",
                                Routes.API + Routes.User.ROOT + "/**",
                                Routes.Account.ROOT + "/**")
                        .authenticated()
                        // Mặc định: mọi request khác đều phải đăng nhập
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Nếu là API/AJAX -> trả 401
                            if (request.getRequestURI().startsWith("/api/") ||
                                    "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                // Nếu là request trang bình thường -> chuyển về trang chủ
                                response.sendRedirect(request.getContextPath() + "/");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Nếu là API/AJAX -> trả 403
                            if (request.getRequestURI().startsWith("/api/") ||
                                    "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                            } else {
                                // Nếu là request trang bình thường -> chuyển về trang chủ
                                response.sendRedirect(request.getContextPath() + "/");
                            }
                        }));
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

    /**
     * ActiveUserFilter được đánh dấu là @Component nên Spring Boot sẽ tự động đăng ký nó
     * như một servlet filter. Tuy nhiên ta đã thêm nó vào SecurityFilterChain (addFilterBefore)
     * nên cần tắt auto-registration để tránh chạy 2 lần.
     */
    @Bean
    public FilterRegistrationBean<ActiveUserFilter> activeUserFilterRegistration(ActiveUserFilter filter) {
        FilterRegistrationBean<ActiveUserFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
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
