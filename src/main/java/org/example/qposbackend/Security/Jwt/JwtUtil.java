package org.example.qposbackend.Security.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  @Value("${jwt.secret_key}")
  private String secretKey;

  @Value("${jwt.expiration_time}")
  private Long accessTokenValidity;

  public Date getExpirationDateFromToken(String token) {
    return extractAllClaims(token).getExpiration();
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  public String generateToken(SystemUserDetails user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("privileges", user.getAuthorities());
    claims.put("shopCode", user.getShopCode());
    return doGenerateToken(user.getUsername(), claims);
  }

  private String doGenerateToken(String username, Map<String, Object> claims) {
    Long expirationTimeLong = accessTokenValidity;
    final Date createdDate = new Date();
    final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong);

    return Jwts.builder()
        .subject(username)
        .issuedAt(createdDate)
        .expiration(expirationDate)
        .claims(claims)
        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
        .compact();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String getShopCode(String token) {
    return extractClaim(token, claims -> claims.get("shopCode", String.class));
  }

  public Boolean validateToken(String token, SystemUserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
