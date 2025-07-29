package br.com.desafioalura.forumhub.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {
    private final long expiration = 1000 * 60 * 60 * 24; // 24 horas
    @Value("${jwt.secret}")
    private String secret;
    private Algorithm algorithm;

    private Algorithm getAlgorithm() {
        if (this.algorithm == null) {
            this.algorithm = Algorithm.HMAC512(secret);
        }
        return this.algorithm;
    }

    public String generateToken(UserDetails userDetails, String role) {
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(getAlgorithm());
    }

    public String getUsernameFromToken(String token) {
        return decodeToken(token).getSubject();
    }

    public String getEmailFromToken(String token) {
        return decodeToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return decodeToken(token).getClaim("role").asString();
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = decodeToken(token).getExpiresAt();
        return expirationDate.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private DecodedJWT decodeToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }


}
