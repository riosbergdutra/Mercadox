package auth.api.auth.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import auth.api.auth.dto.LoginRequest;
import auth.api.auth.dto.UsuarioDto;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final RestTemplate restTemplate;
    private final long expiryDuration = 300L; // 5 minutes

    @Value("${user.service.url}")
    private String userServiceUrl;

    public JwtService(JwtEncoder jwtEncoder, RestTemplate restTemplate) {
        this.jwtEncoder = jwtEncoder;
        this.restTemplate = restTemplate;
    }

    public String generateToken(UsuarioDto user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.idUsuario())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiryDuration))
                .claim("role", user.role())
                .claim("userId", user.idUsuario())
                .build();
    
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getExpiryDuration() {
        return expiryDuration;
    }

    public UsuarioDto validateUserByCredentials(String email, String senha) {
        String url = String.format("%s/usuarios/email", userServiceUrl);
        LoginRequest loginRequest = new LoginRequest(email, senha);
        System.out.println("Sending request to user service: " + url);
        try {
            ResponseEntity<UsuarioDto> response = restTemplate.postForEntity(url, loginRequest, UsuarioDto.class);
            System.out.println("Response status: " + response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("User found: " + response.getBody());
                return response.getBody();
            } else {
                System.out.println("Failed to validate user, response status: " + response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            // Log the error or handle it as needed
            System.out.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
