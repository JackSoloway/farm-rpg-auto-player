package org.example.auth;

import org.example.security.JwtService;
import org.example.user.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        Object rawPrincipal = authentication.getPrincipal();
        if (!(rawPrincipal instanceof UserDetails principal)) {
            throw new IllegalStateException("Expected UserDetails, got " + rawPrincipal);
        }

        String token = jwtService.createToken(principal);
        String role = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(Role.USER.authority());

        return new TokenResponse(token, principal.getUsername(), role);
    }
}
