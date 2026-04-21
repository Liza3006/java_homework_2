package org.example.spring_hw.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final ObjectMapper objectMapper;
  private final byte[] secret;
  private final Duration ttl;

  public JwtUtils(ObjectMapper objectMapper,
                  @Value("${app.security.jwt-secret}") String secret,
                  @Value("${app.security.jwt-ttl}") Duration ttl) {
    this.objectMapper = objectMapper;
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.ttl = ttl;
  }

  public String generateToken(Authentication authentication) {
    try {
      Map<String, Object> header = new LinkedHashMap<>();
      header.put("alg", "HS256");
      header.put("typ", "JWT");

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("sub", authentication.getName());
      payload.put("exp", Instant.now().plus(ttl).getEpochSecond());
      payload.put("authorities", authentication.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .toList());

      String headerPart = encodeJson(header);
      String payloadPart = encodeJson(payload);
      String signature = sign(headerPart + "." + payloadPart);
      return headerPart + "." + payloadPart + "." + signature;
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot create JWT", ex);
    }
  }

  public JwtPrincipal validate(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("JWT must contain three parts");
      }
      String expected = sign(parts[0] + "." + parts[1]);
      if (!constantTimeEquals(expected, parts[2])) {
        throw new IllegalArgumentException("Invalid JWT signature");
      }

      Map<String, Object> payload = objectMapper.readValue(base64UrlDecode(parts[1]), new TypeReference<>() {});
      Number exp = (Number) payload.get("exp");
      if (exp == null || Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now())) {
        throw new IllegalArgumentException("JWT expired");
      }
      Object rawAuthorities = payload.get("authorities");
      List<String> authorities = objectMapper.convertValue(rawAuthorities, new TypeReference<>() {});
      return new JwtPrincipal((String) payload.get("sub"), authorities);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid JWT", ex);
    }
  }

  public String mask(String token) {
    if (token == null || token.length() <= 12) {
      return "******";
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
  }

  private String encodeJson(Map<String, Object> value) throws Exception {
    return base64UrlEncode(objectMapper.writeValueAsBytes(value));
  }

  private String sign(String data) throws Exception {
    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
    return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
  }

  private boolean constantTimeEquals(String left, String right) {
    byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
    byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
    if (leftBytes.length != rightBytes.length) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < leftBytes.length; i++) {
      result |= leftBytes[i] ^ rightBytes[i];
    }
    return result == 0;
  }

  private String base64UrlEncode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private byte[] base64UrlDecode(String value) {
    return Base64.getUrlDecoder().decode(value);
  }

  public static class JwtPrincipal {
    private final String username;
    private final List<String> authorities;

    JwtPrincipal(String username, List<String> authorities) {
      this.username = username;
      this.authorities = authorities;
    }

    public String getUsername() {
      return username;
    }

    public List<String> getAuthorities() {
      return authorities;
    }
  }
}
