package com.example.zongshe1.modules.ops.cache;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class ProductListCacheFilter extends OncePerRequestFilter {

    private static final String PREFIX = "/api/products";
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final ProductCacheProperties props;
    private final Cache<String, byte[]> productListCache;
    private final ProductCacheMetrics metrics;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!props.isEnabled() || !isProductApi(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method) && isProductList(request.getRequestURI())) {
            String key = cacheKey(request);
            byte[] cached = productListCache.getIfPresent(key);
            if (cached != null) {
                metrics.hit();
                log.info("product-cache HIT key={} hits={} misses={}", key, metrics.hits(), metrics.misses());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getOutputStream().write(cached);
                return;
            }

            metrics.miss();
            log.info("product-cache MISS key={} hits={} misses={}", key, metrics.hits(), metrics.misses());
            ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrapped);
            if (wrapped.getStatus() == HttpServletResponse.SC_OK) {
                byte[] body = wrapped.getContentAsByteArray();
                if (body.length > 0) {
                    productListCache.put(key, body);
                }
            }
            wrapped.copyBodyToResponse();
            return;
        }

        if (WRITE_METHODS.contains(method.toUpperCase())) {
            ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrapped);
            int status = wrapped.getStatus();
            if (status >= 200 && status < 300) {
                productListCache.invalidateAll();
                metrics.invalidate();
                log.info("product-cache INVALIDATE after {} {} status={}", method, request.getRequestURI(), status);
            }
            wrapped.copyBodyToResponse();
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProductApi(String uri) {
        return uri != null && (PREFIX.equals(uri) || uri.startsWith(PREFIX + "/"));
    }

    private boolean isProductList(String uri) {
        return PREFIX.equals(uri);
    }

    private String cacheKey(HttpServletRequest request) {
        String qs = request.getQueryString();
        return request.getMethod() + "|" + request.getRequestURI() + "|" + (qs == null ? "" : qs);
    }
}