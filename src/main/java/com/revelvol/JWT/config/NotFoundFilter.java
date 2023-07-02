package com.revelvol.JWT.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.revelvol.JWT.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

@Component
@Order(1)
public class NotFoundFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public NotFoundFilter(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!isUriExists(request)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // Create a custom error response
            String message = "The requested resource was not found";
            ApiResponse errorResponse = new ApiResponse(HttpStatus.NOT_FOUND.value(), message);

            // Write the error response to the output stream
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        filterChain.doFilter(request,response);

    }

    private boolean isUriExists(HttpServletRequest request) {
        HandlerExecutionChain handlerExecutionChain;
        try {
            handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);
        } catch (Exception e) {
            // Exception occurred, indicating that the URI doesn't exist
            return false;
        }
        return handlerExecutionChain != null && handlerExecutionChain.getHandler() != null;
    }
}
