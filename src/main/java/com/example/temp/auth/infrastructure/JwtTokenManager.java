package com.example.temp.auth.infrastructure;

import com.example.temp.auth.dto.response.TokenInfo;
import com.example.temp.auth.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class JwtTokenManager implements TokenManager {

    private static final String BEARER = "Bearer";

    private final Clock clock;
    private final JwtProperties properties;
    private final SecretKey key;
    private final JwtParser parser;

    /**
     * 해당 객체가 생성되면, SecretKey와 JwtParser 객체가 함께 생성됩니다.
     *
     * @param clock
     * @param properties
     */
    @Autowired
    public JwtTokenManager(Clock clock, JwtProperties properties) {
        this.clock = clock;
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.parser = Jwts.parser()
            .verifyWith(key)
            .clock(jwtClock())
            .build();
    }

    /**
     * jjwt에서 제공하는 JwtParser에서 사용할 Clock을 생성합니다. 이를 통해 JwtParser가 '현재 시간'에 종속되지 않도록 만듭니다.
     */
    private io.jsonwebtoken.Clock jwtClock() {
        return () -> Date.from(Instant.now(clock));
    }

    /**
     * 입력받은 id를 사용해 Access Token, Refresh Token을 발급합니다.
     *
     * @param id
     * @return
     */
    @Override
    public TokenInfo issue(Long id) {
        LocalDateTime now = LocalDateTime.now(clock);
        String accessToken = makeToken(String.valueOf(id), properties.accessTokenExpires(), now);
        String refreshToken = makeToken(String.valueOf(id), properties.refreshTokenExpires(), now);
        return TokenInfo.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    private String makeToken(String sub, long expires, LocalDateTime now) {
        LocalDateTime expiresDateTime = now.plusSeconds(expires);
        return Jwts.builder()
            .subject(sub)
            .expiration(Timestamp.valueOf(expiresDateTime))
            .signWith(key)
            .compact();
    }

    /**
     * 입력받은 refreshToken을 사용해 accessToken과 refreshToken을 재발급합니다.
     *
     * @param refreshToken
     * @return
     * @throws TokenInvalidException 토큰의 서명이 적절하지 않을 때, 해당 Exception이 발생합니다.
     * @throws ExpiredJwtException refreshToken이 만료되었을 때, 해당 Exception이 발생합니다.
     */
    @Override
    public TokenInfo reIssue(String refreshToken) {
        Jws<Claims> claimsJws = parse(refreshToken);
        Claims claims = claimsJws.getPayload();
        long id = Long.parseLong(claims.getSubject());
        return issue(id);
    }

    private Jws<Claims> parse(String token) {
        try {
            return parser.parseSignedClaims(token);
        } catch (SignatureException e) {
            throw new TokenInvalidException(e);
        }
    }
}
