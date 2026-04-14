package com.innowise.gatewayservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for working with JWT tokens in the API Gateway.
 * <p>
 * Provides methods for:
 * <ul>
 *   <li>validating token signature and expiration</li>
 *   <li>extracting user-specific claims (userId, role)</li>
 *   <li>parsing token payload using the configured signing key</li>
 * </ul>
 * The class relies on a Base64-encoded secret key provided via application configuration.
 */
@Component
public class JwtUtil {

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";

  @Value("${jwt.secret}")
  private String secret;

  /**
   * Builds the HMAC signing key from the Base64-encoded secret.
   *
   * @return the signing {@link Key}
   */
  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Validates the JWT token by checking its signature and expiration time.
   *
   * @param token the JWT token
   * @return true if the token is valid and not expired, false otherwise
   */
  public boolean isValid(String token) {
    try {
      Claims claims = extractAllClaims(token);
      return !isExpired(claims);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extracts the user ID from the token claims.
   *
   * @param token the JWT token
   * @return the user ID as a string
   */
  public String getUserId(String token) {
    Claims claims = extractAllClaims(token);
    return String.valueOf(claims.get(CLAIM_USER_ID));
  }

  /**
   * Extracts the user role from the token claims.
   *
   * @param token the JWT token
   * @return the role value
   */
  public String getRole(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get(CLAIM_ROLE, String.class);
  }

  /**
   * Checks whether the token is expired.
   *
   * @param claims the parsed JWT claims
   * @return true if the token expiration date is before the current time
   */
  private boolean isExpired(Claims claims) {
    Date expiration = claims.getExpiration();
    return expiration.before(new Date());
  }

  /**
   * Parses and returns all claims from the JWT token.
   *
   * @param token the JWT token
   * @return the extracted {@link Claims}
   */
  private Claims extractAllClaims(String token) {
    return Jwts
            .parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }
}
