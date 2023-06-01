package com.revelvol.JWT.service;

import com.revelvol.JWT.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {
    // todo move secret key to environment variable
    // use hex as best practice and easier implementation
    private static final String SECRET_KEY = "743777217A25432A462D4A614E645266556A586E3272357538782F413F442847";
    public String extractUsername(String jwt) {
        return extractClaims(jwt , Claims::getSubject);
        //look at that :: getsubject, jadi ini shorthand methjod to overiide apply di claiim resoilver diabawah
    }

    // a generate token method with extra claims
    public String generateToken(
            Map<String, Objects> extraClaims,
            UserDetails userDetails // take user detail from spring framework
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24 ))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // generate token without extra claims
    public String generateToken(
            UserDetails userDetails
    ) {
        return generateToken(new HashMap<>(),userDetails);
    }

    // method to validate token
    public boolean isTokenValid(String jwt, User userDetails) {
        final String username = extractUsername(jwt);
        return (username.equals(userDetails.getEmail()) && isTokenExpired(jwt));

    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).after(new Date(System.currentTimeMillis()));
    }

    private Date extractExpiration(String jwt) {
        return extractClaims(jwt, Claims::getExpiration);
    }

    // extract single claims every pass
    // in case wondering <> is a generic type, jadi bisa work with all data type
    // function also the same , jadi ini take claims and return T
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        //kayaknya ini just some kind of java thing buat execute function
        return claimsResolver.apply(claims);
    }
    // extract all claims from the token
    private Claims extractAllClaims(String token) {

        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    //get the signin key, it return a key type that the jsot will use
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        //hjmacshakey are the algorithm to implement the jsot
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
