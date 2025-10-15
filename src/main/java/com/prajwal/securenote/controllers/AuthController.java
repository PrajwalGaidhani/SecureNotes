package com.prajwal.securenote.controllers;


import ch.qos.logback.core.encoder.EchoEncoder;
import com.prajwal.securenote.models.AppRole;
import com.prajwal.securenote.models.Role;
import com.prajwal.securenote.models.User;
import com.prajwal.securenote.repositories.RoleRepository;
import com.prajwal.securenote.repositories.UserRepository;
import com.prajwal.securenote.security.jwt.JwtUtils;
import com.prajwal.securenote.security.request.LoginRequest;
import com.prajwal.securenote.security.response.LoginResponse;
import com.prajwal.securenote.security.request.SignupRequest;
import com.prajwal.securenote.security.response.MessageResponse;
import com.prajwal.securenote.security.response.UserInfoEndpointConfig;
import com.prajwal.securenote.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.annotation.web.oauth2.login.UserInfoEndpointDsl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @PostMapping(
            value = "/public/signin"
    )
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication =authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
        }catch (AuthenticationException e){
            Map<String,Object> map=new HashMap<>();
            map.put("message","Invalid credentials");
            map.put("status",false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
        //Set the authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails=(UserDetails) authentication.getPrincipal();
        String jwtToken=jwtUtils.generateTokenFromUsername(userDetails);
        //Collect role from userDetails
        List<String> roles=userDetails.getAuthorities().stream().map(item->item.getAuthority()).collect(Collectors.toList());
        // Preapre the response body, now inculding the jwt token diretly in the body
        LoginResponse loginResponse=new LoginResponse(jwtToken,userDetails.getUsername(),roles);
        return  ResponseEntity.ok(loginResponse);
    }
    @GetMapping("/public/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/public/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByUserName(signupRequest.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }
        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }
        // create user account

        User user=new User(signupRequest.getUsername(),signupRequest.getEmail(),encoder.encode(signupRequest.getPassword()));
        Set<String> strRoles=signupRequest.getRoles();
        Role role;
        if(strRoles==null || strRoles.isEmpty()){
            role=roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(()->new RuntimeException("Error: Role is not found."));
        }else{
            String r=strRoles.iterator().next();
            if(r.equals("admin")){
                role=roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow(()->new RuntimeException("Error: Role is not found."));
            }else{
                role=roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(()->new RuntimeException("Error: Role is not found."));
            }
        }
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setTwoFactorEnabled(false);
        user.setAccountExpiryDate(LocalDate.now().plusYears(1));
        user.setCredentialsExpiryDate(LocalDate.now().plusYears(1));
        user.setSignUpMethod("email");
        userRepository.save(user);
        // return
        return ResponseEntity.ok(new MessageResponse("user register successifully"));
    }


    @GetMapping("/user")
    public  ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails userDetails){
        User user=userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()->new RuntimeException("User not found"));
        List<String> userRole=userDetails.
                getAuthorities().stream().
                map(item->item.getAuthority()).collect(Collectors.toList());
        UserInfoEndpointConfig response =new UserInfoEndpointConfig(user.getUserId(),user.getUserName(),user.getEmail(),user.isAccountNonLocked(),user.isAccountNonExpired(),user.isCredentialsNonExpired(),user.isEnabled(),user.getCredentialsExpiryDate(),user.getAccountExpiryDate(),user.isTwoFactorEnabled(),userRole);
        return ResponseEntity.ok(userDetails);
    }
    @GetMapping("/username")
    public String getCurrentUserName(@AuthenticationPrincipal UserDetails userDetails){
        return (userDetails!=null)?userDetails.getUsername():"no name";
    }
}
