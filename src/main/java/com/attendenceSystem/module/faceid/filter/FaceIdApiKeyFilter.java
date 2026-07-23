package com.attendenceSystem.module.faceid.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FaceIdApiKeyFilter extends OncePerRequestFilter {

    @Value("${face-id.api-key}")
    private String apiKey;

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String FACE_ID_PATH_PREFIX = "/api/face-id";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(FACE_ID_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // String requestApiKey = extractApiKey(request);

        // if (apiKey == null || apiKey.isBlank() || !apiKey.equals(requestApiKey)) {
        //     throw new RuntimeException("Invalid or missing API key");
        // }

        filterChain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) {
        String apiKeyHeader = request.getHeader(API_KEY_HEADER);
        if (apiKeyHeader != null && apiKeyHeader.startsWith("Bearer ")) {
            return apiKeyHeader.substring(7);
        }
        return apiKeyHeader;
    }
}