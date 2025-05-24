package org.example.qposbackend.Security.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.SystemUserDetails.UserDetailsServiceImpl;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopRepository;
import org.example.qposbackend.Authorization.authentication.UsernamePasswordShopCodeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final UserShopRepository userShopRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    String token = null;
    String username = null;
    String shopCode = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
      username = jwtUtil.extractUsername(token);
      shopCode = jwtUtil.getShopCode(token);
    }

    if (username != null
        && shopCode != null
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      Optional<UserShop> userShopOptional =
          userShopRepository.findUserShopByShop_CodeAndUser_email(username, shopCode);
      SystemUserDetails userDetails = new SystemUserDetails(userShopOptional.get());
      if (jwtUtil.validateToken(token, userDetails)) {
        UsernamePasswordShopCodeAuthenticationToken authToken =
            new UsernamePasswordShopCodeAuthenticationToken(
                userDetails, null, shopCode, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    filterChain.doFilter(request, response);
  }
}
