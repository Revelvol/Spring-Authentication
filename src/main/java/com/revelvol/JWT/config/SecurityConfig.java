package com.revelvol.JWT.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
// we want to add/ implement the jwt authentication filter to the normal java security filter chain

public class SecurityConfig {


    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final NotFoundFilter notFoundFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider, NotFoundFilter notFoundFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.notFoundFilter = notFoundFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf()
                .disable()
                // endpoint whitelist that do not need authentication
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/auth/**","/") //all the list requestin here will permit all
                .permitAll()
                .anyRequest()// any other request need to be authenticated
                .authenticated()
                .and() // add new configuration
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //add new session for each request
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(notFoundFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthFilter, NotFoundFilter.class) // we want jwt filter to do first before the password
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
                
        return http.build();
    }

}
