package com.revelvol.JWT.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.function.Function;

@Service
public class JwtService {
    // this is the secret key for jwot, implement environment for this
    // use heex as best practice and easier implementation
    private static final String SECRET_KEY = "743777217A25432A462D4A614E645266556A586E3272357538782F413F442847";
    public String extractUsername(String jwt) {
        return "helo";
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
                .parseClaimsJwt(token)
                .getBody();
    }
    //get the signin key, it return a key type that the jsot will use
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        //hjmacshakey are the algorithm to implement the jsot
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
