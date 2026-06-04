package com.example.demo.filter;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs exactly ONCE per HTTP request.
 *
 * HOW IT WORKS:
 *   1. Read the "Authorization" header.
 *   2. If it starts with "Bearer ", extract the token.
 *   3. Parse the email (subject) from the token.
 *   4. Load the full UserDetails from the database using that email.
 *   5. Validate the token (signature + expiry + email match).
 *   6. If valid, set the authenticated principal in the SecurityContext
 *      so downstream controllers can call SecurityContextHolder.getContext()
 *      .getAuthentication().getPrincipal() to get the logged-in user.
 *
 * If anything fails (missing header, bad signature, expired), the filter
 * simply doesn't authenticate — the request proceeds as anonymous and
 * Spring Security will return 403 for protected endpoints.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No Bearer token present — skip this filter, proceed as anonymous
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token string after "Bearer "
        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtService.extractEmail(jwt);

            // Only authenticate if not already authenticated in this request
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load the full user object from the DB to verify account is still active
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Build an authentication token and set it in the SecurityContext
                    // This is what makes Spring Security treat this request as authenticated
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,               // credentials — null because JWT, no password needed
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token parsing failed (bad signature, expired, malformed) — log and continue as anonymous
            System.out.println("⚠️ JWT validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
