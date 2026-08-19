package com.example.walletxbackend.util;

import com.example.walletxbackend.entity.User;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Minimal JWT utility that builds and validates a token using HMAC‑SHA256.
 * <p>
 * This implementation avoids external JSON libraries to keep the project
 * lightweight. It manually constructs the JSON payload and parses the
 * required fields (username and expiration) with simple string operations.
 * <p>
 * For production use replace this with a dedicated library (e.g., jjwt) and
 * store the secret securely.
 */
@Component
public class JwtUtil {

    private static final String SECRET = "ChangeThisSecretKeyToStrongRandomValue"; // TODO: externalize
    private static final long EXPIRATION_MS = 86_400_000L; // 24 hours
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    public String generateToken(User user) {

        long nowSec = Instant.now().getEpochSecond();
        long expSec = nowSec + EXPIRATION_MS / 1000;

        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"role\":\"%s\",\"tokenVersion\":%d,\"iat\":%d,\"exp\":%d}",
                user.getUsername(),
                user.getRole().name(),
                user.getTokenVersion(),
                nowSec,
                expSec
        );

        String encodedHeader =
                base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));

        String encodedPayload =
                base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        String unsigned = encodedHeader + "." + encodedPayload;

        return unsigned + "." + sign(unsigned);
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String unsigned = parts[0] + "." + parts[1];
            if (!sign(unsigned).equals(parts[2])) return false;
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            long exp = parseLong(payloadJson, "exp");
            long nowSec = Instant.now().getEpochSecond();
            return nowSec < exp;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            return parseString(payloadJson, "sub");
        } catch (Exception e) {
            return null;
        }
    }

    // Simple JSON value extraction helpers – not a full parser.
    private long parseLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return -1;
        int start = idx + search.length();
        int end = json.indexOf(',', start);
        if (end == -1) end = json.indexOf('}', start);
        String val = json.substring(start, end).trim();
        return Long.parseLong(val);
    }

    private String parseString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int start = idx + search.length();
        int end = json.indexOf('\"', start);
        return json.substring(start, end);
    }

    public Long extractTokenVersion(String token) {

        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return null;
            }

            String payloadJson = new String(
                    base64UrlDecode(parts[1]),
                    StandardCharsets.UTF_8
            );

            return parseLong(payloadJson, "tokenVersion");

        } catch (Exception e) {
            return null;
        }
    }
    /*
        String search = \"\"" + key + \"\":\";
        int idx = json.indexOf(search);
        if (idx == -1) return -1;
        int start = idx + search.length();
        int end = json.indexOf(',', start);
        if (end == -1) end = json.indexOf('}', start);
        String val = json.substring(start, end).trim();
        return Long.parseLong(val);
    }

    private String parseString(String json, String key) {
        String search = \"\"" + key + \"\":\"\";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int start = idx + search.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    */
}
