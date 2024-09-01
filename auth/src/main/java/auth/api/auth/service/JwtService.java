package auth.api.auth.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import auth.api.auth.dto.UsuarioDto;
import auth.api.auth.dto.LoginResponse;
import auth.api.auth.exceptions.InvalidCredentialsException;
import auth.api.auth.exceptions.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RestTemplate restTemplate;
    private final long accessTokenExpiryDuration = 300L; // 5 minutos
    private final long refreshTokenExpiryDuration = 86400L; // 24 horas

    @Value("${user.service.url}")
    private String userServiceUrl;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, RestTemplate restTemplate) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.restTemplate = restTemplate;
    }

    // Método para realizar login e gerar tokens
    public LoginResponse loginUser(String email, String senha, HttpServletResponse response) {
        UsuarioDto usuarioDto = validateUserByCredentials(email, senha);

        if (usuarioDto == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = generateAccessToken(usuarioDto);
        String refreshToken = generateRefreshToken(usuarioDto);

        setRefreshTokenCookie(refreshToken, response);

        return new LoginResponse(accessToken, refreshToken, accessTokenExpiryDuration);
    }

    // Método para atualizar o token de acesso (refresh)
    public LoginResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        if (!validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        UsuarioDto usuarioDto = getUserFromRefreshToken(refreshToken);
        if (usuarioDto == null) {
            throw new InvalidRefreshTokenException("User not found");
        }

        String newAccessToken = generateAccessToken(usuarioDto);
        setRefreshTokenCookie(refreshToken, response); // Renova o cookie do refresh token

        return new LoginResponse(newAccessToken, refreshToken, accessTokenExpiryDuration);
    }

    // Método para definir o cookie do refresh token
    private void setRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Use true em produção
        refreshTokenCookie.setPath("/auth/refresh");
        refreshTokenCookie.setMaxAge((int) refreshTokenExpiryDuration);
        response.addCookie(refreshTokenCookie);
    }

    // Método para gerar token de acesso (Access Token)
    public String generateAccessToken(UsuarioDto user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.idUsuario())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiryDuration))
                .claim("role", user.role())
                .claim("userId", user.idUsuario())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // Método para gerar token de atualização (Refresh Token)
    public String generateRefreshToken(UsuarioDto user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.idUsuario())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpiryDuration))
                .claim("userId", user.idUsuario())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // Método para validar o refresh token
    public boolean validateRefreshToken(String refreshToken) {
        try {
            jwtDecoder.decode(refreshToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Método para obter o usuário a partir do refresh token
    public UsuarioDto getUserFromRefreshToken(String refreshToken) {
        try {
            Jwt decodedToken = jwtDecoder.decode(refreshToken);
            String userId = decodedToken.getSubject();
            return fetchUserById(userId);
        } catch (JwtException e) {
            return null;
        }
    }

    // Método para buscar usuário por ID
    public UsuarioDto fetchUserById(String userId) {
        String url = String.format("%s/usuario/%s", userServiceUrl, userId);
        try {
            ResponseEntity<UsuarioDto> response = restTemplate.getForEntity(url, UsuarioDto.class);
            return response.getBody();
        } catch (RestClientException e) {
            // Log or handle client errors as needed
            return null;
        }
    }

    // Método para validar credenciais do usuário
    public UsuarioDto validateUserByCredentials(String email, String senha) {
        String url = String.format("%s/usuario/email/", userServiceUrl);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", email);
        params.add("senha", senha);

        String uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(params)
                .toUriString();

        try {
            ResponseEntity<UsuarioDto> response = restTemplate.getForEntity(uri, UsuarioDto.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            // Log or handle client errors as needed
        }
        return null;
    }

    // Getter para a duração de expiração do token de acesso
    public long getAccessTokenExpiryDuration() {
        return accessTokenExpiryDuration;
    }

    // Getter para a duração de expiração do refresh token
    public long getRefreshTokenExpiryDuration() {
        return refreshTokenExpiryDuration;
    }
}
