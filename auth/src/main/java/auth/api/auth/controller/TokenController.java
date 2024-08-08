package auth.api.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import auth.api.auth.dto.LoginRequest;
import auth.api.auth.dto.LoginResponse;
import auth.api.auth.dto.UsuarioDto;
import auth.api.auth.service.JwtService;

@RestController
@RequestMapping("/auth")
public class TokenController {
    
    private final JwtService jwtService;

    public TokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        UsuarioDto usuarioDto = jwtService.validateUserByCredentials(loginRequest.email(), loginRequest.senha());
        
        if (usuarioDto == null) {
            throw new BadCredentialsException("User does not exist!");
        }

        if (!usuarioDto.senha().equals(loginRequest.senha())) {
            throw new BadCredentialsException("User or password is invalid!");
        }

        var jwt = jwtService.generateToken(usuarioDto);

        return ResponseEntity.ok(new LoginResponse(jwt, jwtService.getExpiryDuration()));
    }
}
