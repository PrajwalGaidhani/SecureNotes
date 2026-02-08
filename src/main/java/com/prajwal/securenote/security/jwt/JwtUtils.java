package com.prajwal.securenote.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecreat}")
    private String jwtSecreat;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken=request.getHeader("Authorization");
        logger.debug("JWT Header: {}",bearerToken);
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
    public Key key(){
//        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecreat));
        return Keys.hmacShaKeyFor(jwtSecreat.getBytes(StandardCharsets.UTF_8));

    }
// c
    public String generateTokenFromUsername(UserDetails userDetails){
        String username=userDetails.getUsername();
        logger.debug("JWT Token for username: {}",username);
//         Collect roles from userDetails
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
//        return Jwts.builder().subject(username) .claim("roles", roles) .issuedAt(new Date()).expiration(new Date((new Date()).getTime()+jwtExpirationMs)).signWith(key()).compact();
        return Jwts.builder().subject(username) .issuedAt(new Date()).expiration(new Date((new Date()).getTime()+jwtExpirationMs)).signWith(key()).compact();


    }

    public  String getUserNameFromJwtToken(String token){
        return Jwts.parser().verifyWith((SecretKey)  key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("validateJwtToken:");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        }catch (MalformedJwtException e){
            logger.error("Invalid JWT Token {}",e.getMessage());
        }catch (ExpiredJwtException e){
            logger.error("Invalid JWT Token {}",e.getMessage());
        }catch (UnsupportedJwtException e){
            logger.error("Invalid JWT Token {}",e.getMessage());
        }catch (IllegalArgumentException e){
            logger.error("Invalid JWT Token {}",e.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
