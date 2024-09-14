package auth.api.auth.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import auth.api.auth.dto.UsuarioDto;
import auth.api.auth.exceptions.InvalidCredentialsException;
import auth.api.auth.exceptions.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RestTemplate restTemplate;
    private final long accessTokenExpiryDuration = 300L; // 5 minutes
    private final long refreshTokenExpiryDuration = 2592000L; // 30 days

    @Value("${user.service.url}")
    private String userServiceUrl;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, RestTemplate restTemplate) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.restTemplate = restTemplate;
    }

    public String generateAccessToken(UsuarioDto user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.idUsuario())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiryDuration))
                .claim("scope", user.role())
                .claim("userId", user.idUsuario())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(UsuarioDto user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.idUsuario())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpiryDuration))
                .claim("userId", user.idUsuario())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public void setTokensInCookies(HttpServletResponse response, String accessToken, String refreshToken) {
    try {
        // Configura e adiciona o cookie de accessToken
        CookieService.setCookie(response, "accessToken", accessToken, (int) accessTokenExpiryDuration);

        // Configura e adiciona o cookie de refreshToken
        CookieService.setCookie(response, "refreshToken", refreshToken, (int) refreshTokenExpiryDuration);
    } catch (IOException e) {
        e.printStackTrace(); // Log da exceção
    }
}


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
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new InvalidCredentialsException("Failed to validate user");
            }
        } catch (HttpClientErrorException e) {
            throw new InvalidCredentialsException(
                    "HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while validating user", e);
        }
    }

    public UsuarioDto validateUserByRefreshToken(String refreshToken) {
        try {
            Jwt decodedJwt = jwtDecoder.decode(refreshToken);

            // Verificar a expiração do token
            if (decodedJwt.getExpiresAt().isBefore(Instant.now())) {
                throw new InvalidRefreshTokenException("Refresh token is expired");
            }

            String userId = decodedJwt.getSubject();
            // Aqui você pode adicionar lógica para buscar o usuário no banco de dados se
            // necessário.
            UsuarioDto usuarioDto = findUserById(userId);

            if (usuarioDto == null) {
                throw new InvalidRefreshTokenException("Refresh token is invalid");
            }

            return usuarioDto;
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Failed to validate refresh token");
        }
    }

    private UsuarioDto findUserById(String userId) {
        String url = String.format("%s/usuario/token/%s", userServiceUrl, userId);

        try {
            ResponseEntity<UsuarioDto> response = restTemplate.getForEntity(url, UsuarioDto.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                return null;
            }
        } catch (HttpClientErrorException e) {
            return null;
        }
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Optional<String> refreshTokenOpt = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();

        if (refreshTokenOpt.isEmpty()) {
            throw new InvalidRefreshTokenException("Refresh token is missing");
        }

        String refreshToken = refreshTokenOpt.get();
        UsuarioDto usuarioDto = validateUserByRefreshToken(refreshToken);

        String newAccessToken = generateAccessToken(usuarioDto);
        String newRefreshToken = generateRefreshToken(usuarioDto);

        setTokensInCookies(response, newAccessToken, newRefreshToken);
    }

    public long getAccessTokenExpiryDuration() {
        return accessTokenExpiryDuration;
    }

    public long getRefreshTokenExpiryDuration() {
        return refreshTokenExpiryDuration;
    }

}
