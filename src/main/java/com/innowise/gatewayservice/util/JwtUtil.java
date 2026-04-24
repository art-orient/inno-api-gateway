package com.innowise.gatewayservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";

  @Value("${jwt.secret}")
  private String secret;

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public boolean isValid(String token) {
    log.info("JwtUtil: validating token " + token);
    try {
      Claims claims = extractAllClaims(token);
      return !isExpired(claims);
    } catch (Exception e) {
      log.error("JWT VALIDATION ERROR", e);
      return false;
    }
  }

  public String getUserId(String token) {
    Claims claims = extractAllClaims(token);
    return String.valueOf(claims.get(CLAIM_USER_ID));
  }

  public String getRole(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get(CLAIM_ROLE, String.class);
  }

  private boolean isExpired(Claims claims) {
    Date expiration = claims.getExpiration();
    return expiration.before(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts
            .parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }
}
