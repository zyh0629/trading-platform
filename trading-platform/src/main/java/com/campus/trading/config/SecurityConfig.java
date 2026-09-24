package com.campus.trading.config;

import com.campus.trading.security.JwtAuthenticationFilter;
import com.campus.trading.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/login", "/api/user/register",
                                "/api/user/security-question", "/api/user/reset-by-security",
                                "/upload/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/product/list", "/api/product/{id}",
                                "/api/product/search", "/api/product/category/{categoryId}").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> {
                            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    "{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                                    "{\"code\":403,\"message\":\"没有权限访问\",\"data\":null}");
                        }));
        return http.build();
    }

    private static void writeJsonError(HttpServletResponse response, int status, String body)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}
