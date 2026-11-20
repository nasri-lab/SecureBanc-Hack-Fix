package ma.securebank.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.UserRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final Algorithm algorithm;

    public JwtService(@Value("${securebank.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String create(UserRow user) {
        return JWT.create()
                .withSubject(String.valueOf(user.id()))
                .withClaim("username", user.username())
                .withClaim("role", user.role())
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);
    }

    public AuthUser verify(String token) {
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        return new AuthUser(
                Long.parseLong(jwt.getSubject()),
                jwt.getClaim("username").asString(),
                jwt.getClaim("role").asString());
    }
}
