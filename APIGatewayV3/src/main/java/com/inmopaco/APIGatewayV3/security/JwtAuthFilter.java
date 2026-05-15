package com.inmopaco.APIGatewayV3.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Log4j2
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret:dev-secret-please-change-in-production}")
    private String jwtSecret;

    @Value("${app.environment:production}")
    private String appEnvironment;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if ("local".equals(appEnvironment)) {
            log.info("[filter] Modo LOCAL detectado. Saltando validación JWT para path: {}", path);
            return chain.filter(exchange);
        }

        log.info("[filter] New Gateway Call");
        log.info("[filter] Requested path: {}", path);

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[filter] Request rejected. Missing JWT token in Authorization header (Expected 'Bearer <token>')");
            return blockRequest(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        log.info("[filter] Intercepted JWT: {}", token);

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("[filter] Request rejected. Invalid JWT format");
                return blockRequest(exchange, HttpStatus.UNAUTHORIZED);
            }

            if (!verifySignature(parts[0], parts[1], parts[2])) {
                 log.warn("[filter] Request rejected. Invalid JWT signature");
                 return blockRequest(exchange, HttpStatus.UNAUTHORIZED);
            }

            // payload base64
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            log.info("[filter] Decrypted JWT payload: {}", payloadJson);

            // parsear JSON para obtener atributos
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            String role = (String) payload.get("role");

            if ("admin".equals(role)) {
                log.info("[filter] Validation successful. User role 'admin' detected. Access granted to all services.");
                return chain.filter(exchange);
            } else {
                log.info("[filter] User role '{}' detected. Verifying restrictive routing rules...", role);
                // Si no es admin, solo se permite acceso a las rutas que correspondan al BFF
                if (path.startsWith("/bff/")) {
                    log.info("[filter] Access granted to BFF path: {}", path);
                    return chain.filter(exchange);
                } else {
                    log.warn("[filter] Access denied. User with role {} lacks permission to access general services.", role);
                    return blockRequest(exchange, HttpStatus.FORBIDDEN);
                }
            }
        } catch (Exception e) {
            log.error("[filter] Unexpected error processing JWT: {}", e.getMessage());
            return blockRequest(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean verifySignature(String header, String payload, String signature) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String data = header + "." + payload;
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // base64Url sin padding (estandar JWT)
            String expectedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("[verifySignature] Error during cryptographic validation of token signature", e);
            return false;
        }
    }

    private Mono<Void> blockRequest(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // orden muy bajo -1 para que sea el primer filtro en la cadena
        return -1; 
    }
}
