package com.prajwal.securenote.security.jwt;

import com.prajwal.securenote.security.services.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger= LoggerFactory.getLogger(AuthTokenFilter.class);

    private String parseJwt(HttpServletRequest request){
        String token=jwtUtils.getJwtFromHeader(request);
        logger.debug("JWT Token: {}",token);
        return token;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for uri: {}",request.getRequestURI());
        try{
            String jwt=parseJwt(request);
            if(jwt!=null && jwtUtils.validateJwtToken(jwt)){
                var claims = jwtUtils.getClaims(jwt);
                String username=jwtUtils.getUserNameFromJwtToken(jwt);
                @SuppressWarnings("unchecked")
                var roles = (List<String>) claims.get("roles");
                if (roles == null) roles = List.of(); // ✅ Prevent NullPointerException

                var authorities = roles.stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList();

                UserDetails userDetails=userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userDetails,null,authorities);
                logger.debug("Role from JWT :{}",userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}",e.getMessage());
        }
        filterChain.doFilter(request,response);
    }


}
