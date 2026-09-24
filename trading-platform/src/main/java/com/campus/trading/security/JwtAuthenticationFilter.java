package com.campus.trading.security;

import com.campus.trading.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                Claims claims = jwtUtil.parseToken(token);
                Integer userId = claims.get("userId", Integer.class);
                String username = claims.getSubject();
                Integer role = claims.get("role", Integer.class);
                if (userId == null || username == null || role == null) {
                    throw new IllegalArgumentException("JWT claims are incomplete");
                }
                UserPrincipal principal = new UserPrincipal(userId, username, role);
                String authority = role == 1 ? "ROLE_ADMIN" : "ROLE_USER";
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority(authority)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);
            } catch (Exception ignored) {
                // SecurityContext remains unauthenticated; protected routes return 401.
            }
        }
        filterChain.doFilter(request, response);
    }
}
