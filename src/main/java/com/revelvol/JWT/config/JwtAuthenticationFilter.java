package com.revelvol.JWT.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;

@Component
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;

    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain //do filter in the chain
    ) throws ServletException, IOException {
        //get the authorization where the jwt token reside
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        // do a check if there is auth header  or not start with bearer
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                // pass to another filter
                filterChain.doFilter(request, response);
                return;
            }
            // extract the string after the bearer prefix
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);
            // if user is not null and it is not authenticated, just pass this filter
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // get user from the database, and see whether it is valid or not
                User userDetails = (User) userDetailsService.loadUserByUsername(userEmail);


                if (jwtService.isTokenValid(jwt, userDetails)) {
                    //create token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                            //userDetails.getAuthorities()
                    );
                    // enforce details with the request
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {

            ApiResponse errorResponse = new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                    "JWT token has expired. Please renew the token.");
            HashMap<String, Object> data = new HashMap<>();
            data.put("expired", e.getMessage());

            errorResponse.setData(data);

            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (MalformedJwtException e) {

            ApiResponse errorResponse = new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                    "Token is not a valid JWT");
            HashMap<String, Object> data = new HashMap<>();
            data.put("malformed", e.getMessage());

            errorResponse.setData(data);

            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (SignatureException e) {

            ApiResponse errorResponse = new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                    "Token Signature is invalid");
            HashMap<String, Object> data = new HashMap<>();
            data.put("invalid-exception", e.getMessage());

            errorResponse.setData(data);

            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }

    }
}
