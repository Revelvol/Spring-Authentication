package com.revelvol.JWT.config;

import com.revelvol.JWT.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            // pass to another filter (yang hanle return )
            filterChain.doFilter(request,response);
            return;
        }
        // extract the string after the bearer prefix
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt); // todo extract the user email from the jwt token

    }
}
