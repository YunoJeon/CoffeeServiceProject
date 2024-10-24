package com.coffee.coffeeserviceproject.util;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_USER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_MATCH_TOKEN;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

  @Value("${jwt.secret}")
  private String SECRET_KEY;

  private final MemberRepository memberRepository;

  public String generateToken(String email) {

    Date expirationTime = Date.from(LocalDateTime.now().plusMonths(1).toInstant(ZoneOffset.UTC));

    return JWT.create()
        .withSubject(email)
        .withIssuedAt(new Date())
        .withExpiresAt(expirationTime)
        .sign(Algorithm.HMAC256(SECRET_KEY));
  }

  public String validateToken(String token) {

    JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();
    DecodedJWT decodedJWT = verifier.verify(token);

    return decodedJWT.getSubject();
  }

  public Member getMemberFromEmail(String token) {

    String email;
    try {
      email = validateToken(token);
    } catch (SignatureVerificationException | JWTDecodeException e) {

      throw new CustomException(NOT_MATCH_TOKEN);
    }

    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
  }
}