package com.example.trainerworkloadservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TransactionLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        String txId = LoggingUtils.ensureTransactionId();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            String requestPayload = extractRequestBody(wrappedRequest);
            log.info("tx={} endpoint={} method={} payload={}", txId, request.getRequestURI(), request.getMethod(), requestPayload);
            log.info("tx={} responseStatus={}", txId, wrappedResponse.getStatus());
            wrappedResponse.copyBodyToResponse();
            MDC.remove(LoggingUtils.TRANSACTION_ID_KEY);
        }
    }

    private String extractRequestBody(ContentCachingRequestWrapper requestWrapper) {
        byte[] buf = requestWrapper.getContentAsByteArray();
        if (buf.length == 0) {
            return "<empty>";
        }
        return new String(buf, StandardCharsets.UTF_8);
    }
}

