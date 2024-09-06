package auth.api.auth.controller;

import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import auth.api.auth.dto.LoginRequest;
import auth.api.auth.dto.LoginResponse;
import auth.api.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import auth.api.auth.exceptions.InvalidCredentialsException;
import auth.api.auth.exceptions.InvalidRefreshTokenException;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private final JwtService jwtService;

    public TokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            var usuarioDto = jwtService.validateUserByCredentials(loginRequest.email(), loginRequest.senha());
            String accessToken = jwtService.generateAccessToken(usuarioDto);
            String refreshToken = jwtService.generateRefreshToken(usuarioDto);
            jwtService.setTokensInCookies(response, accessToken, refreshToken);

            // Criar a resposta com os tokens e suas durações
            LoginResponse loginResponse = new LoginResponse(
                accessToken,
                refreshToken
            );

            return ResponseEntity.ok(loginResponse);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            jwtService.refreshToken(request, response);

            // Criar a resposta com o novo access token e refresh token
            // Opcional: retornar o novo access token e refresh token
            // Se não precisar enviar nada, pode usar ResponseEntity.ok().build();
            String newAccessToken = jwtService.generateAccessToken(jwtService.validateUserByRefreshToken(
                getRefreshTokenFromRequest(request)
            ));
            String newRefreshToken = jwtService.generateRefreshToken(jwtService.validateUserByRefreshToken(
                getRefreshTokenFromRequest(request)
            ));

            LoginResponse loginResponse = new LoginResponse(
                newAccessToken,
                newRefreshToken
            );

            return ResponseEntity.ok(loginResponse);
        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                     .filter(cookie -> "refreshToken".equals(cookie.getName()))
                     .map(Cookie::getValue)
                     .findFirst()
                     .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is missing"));
    }
}
