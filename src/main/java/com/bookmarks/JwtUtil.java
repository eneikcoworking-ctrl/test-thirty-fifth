package com.bookmarks;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtil {

    private static String secret = "extremely_long_and_very_secure_secret_key_that_is_at_least_256_bits_long_for_hmac_sha256";
    private static final String ALGORITHM = "HmacSHA256";

    public static void setSecret(String newSecret) {
        if (newSecret != null && !newSecret.trim().isEmpty()) {
            secret = newSecret;
        }
    }

    public static String generateToken(String username) {
        try {
            // Header
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));

            // Payload
            Instant now = Instant.now();
            Instant exp = now.plus(24, ChronoUnit.HOURS);
            String payload = String.format("{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}",
                username, now.getEpochSecond(), exp.getEpochSecond());
            String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

            // Signature
            String signatureInput = encodedHeader + "." + encodedPayload;
            byte[] signatureBytes = hmacSha256(signatureInput, secret);
            String encodedSignature = base64UrlEncode(signatureBytes);

            return signatureInput + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public static String validateTokenAndGetSubject(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String encodedHeader = parts[0];
            String encodedPayload = parts[1];
            String encodedSignature = parts[2];

            // Verify Signature
            String signatureInput = encodedHeader + "." + encodedPayload;
            byte[] expectedSignatureBytes = hmacSha256(signatureInput, secret);
            String expectedSignature = base64UrlEncode(expectedSignatureBytes);

            if (!expectedSignature.equals(encodedSignature)) {
                return null;
            }

            // Decode Payload & Verify Expiration
            String payloadJson = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);

            // Extract exp
            long expValue = extractLongClaim(payloadJson, "exp");
            if (expValue == -1 || expValue < Instant.now().getEpochSecond()) {
                return null; // Expired or missing
            }

            // Extract subject
            return extractStringClaim(payloadJson, "sub");
        } catch (Exception e) {
            return null;
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] hmacSha256(String data, String key) throws Exception {
        Mac sha256HMAC = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        sha256HMAC.init(secretKey);
        return sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String extractStringClaim(String json, String claim) {
        String pattern = "\"" + claim + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private static long extractLongClaim(String json, String claim) {
        String pattern = "\"" + claim + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return -1;
        start += pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) {
            end = json.indexOf("}", start);
        }
        if (end == -1) return -1;
        try {
            return Long.parseLong(json.substring(start, end).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
