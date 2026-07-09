package com.unz.eval.security;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtre de limitation de débit (Rate Limiting).
 *
 * Règles appliquées :
 *  - /api/auth/login : max 5 tentatives / 15 minutes par IP
 *    → Protection contre les attaques par force brute (ENF-5)
 *  - /api/auth/mot-de-passe-oublie : max 3 tentatives / 60 minutes par IP
 *    → Endpoint public et sensible (déclenche un envoi d'e-mail + reset de
 *      mot de passe) : sans cette limite, un abus pourrait griller le quota
 *      d'envoi Gmail (~500/jour) ou spammer une boîte mail ciblée.
 *  - Autres endpoints API : max 100 requêtes / minute par IP
 *    → Protection contre les abus et DDoS
 *
 * Si la limite est dépassée : HTTP 429 Too Many Requests.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.login.capacity:5}")
    private int loginCapacity;

    @Value("${app.rate-limit.login.refill-minutes:15}")
    private long loginRefillMinutes;

    @Value("${app.rate-limit.forgot-password.capacity:3}")
    private int forgotPasswordCapacity;

    @Value("${app.rate-limit.forgot-password.refill-minutes:60}")
    private long forgotPasswordRefillMinutes;

    @Value("${app.rate-limit.api.capacity:100}")
    private int apiCapacity;

    @Value("${app.rate-limit.api.refill-minutes:1}")
    private long apiRefillMinutes;

    // Un bucket par IP pour le login
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    // Un bucket par IP pour "mot de passe oublié" (endpoint public, sensible : envoi d'e-mail + reset)
    private final Map<String, Bucket> forgotPasswordBuckets = new ConcurrentHashMap<>();
    // Un bucket par IP pour l'API générale
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket;
        if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = loginBuckets.computeIfAbsent(ip, k -> buildLoginBucket());
        } else if ((path.equals("/api/auth/mot-de-passe-oublie") || path.equals("/api/auth/reinitialiser-mot-de-passe"))
                && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = forgotPasswordBuckets.computeIfAbsent(ip, k -> buildForgotPasswordBucket());
        } else if (path.startsWith("/api/") && !"OPTIONS".equalsIgnoreCase(request.getMethod())) {
            bucket = apiBuckets.computeIfAbsent(ip, k -> buildApiBucket());
        } else {
            chain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            // log.warn("Rate limit dépassé pour IP={} sur path={}", ip, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\"," +
                "\"message\":\"Trop de tentatives. Réessayez dans " + waitSeconds + " secondes.\"}"
            );
        }
    }

    private Bucket buildLoginBucket() {
        Bandwidth limit = Bandwidth.classic(
                loginCapacity,
                Refill.greedy(loginCapacity, Duration.ofMinutes(loginRefillMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket buildForgotPasswordBucket() {
        Bandwidth limit = Bandwidth.classic(
                forgotPasswordCapacity,
                Refill.greedy(forgotPasswordCapacity, Duration.ofMinutes(forgotPasswordRefillMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket buildApiBucket() {
        Bandwidth limit = Bandwidth.classic(
                apiCapacity,
                Refill.intervally(apiCapacity, Duration.ofMinutes(apiRefillMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
