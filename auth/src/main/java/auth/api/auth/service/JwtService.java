package auth.api.auth.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
    String url = String.format("%s/usuario/email/", userServiceUrl);
    
    // Cria parâmetros de consulta
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("email", email);
    params.add("senha", senha);

    // Constrói a URL com os parâmetros de consulta
    String uri = UriComponentsBuilder.fromHttpUrl(url)
                                 .queryParams(params)
                                 .toUriString();
    
    System.out.println("Sending request to user service: " + uri);
    
    try {
        // Faz a requisição GET com parâmetros de consulta
        ResponseEntity<UsuarioDto> response = restTemplate.getForEntity(uri, UsuarioDto.class);
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