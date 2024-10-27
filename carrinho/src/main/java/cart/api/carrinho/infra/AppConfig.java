package cart.api.carrinho.infra;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();


        restTemplate.setInterceptors(List.of(new AuthInterceptor()));
        
        return restTemplate;
    }
}

class AuthInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Adiciona o token JWT no cabeçalho "Authorization"
        String token = "Bearer " + getToken(); // Método para obter o token
        request.getHeaders().set("Authorization", token);
        
        // Continua a execução da requisição
        return execution.execute(request, body);
    }

    private String getToken() {
        // Obtém o contexto de segurança atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Verifica se a autenticação é do tipo JwtAuthenticationToken
        if (authentication instanceof JwtAuthenticationToken) {
            // Faz o cast para JwtAuthenticationToken e recupera o token
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            return jwtToken.getToken().getTokenValue();
        }
        
        // Caso não seja um JwtAuthenticationToken, lança uma exceção ou retorna um valor apropriado
        throw new IllegalStateException("Autenticação inválida, token JWT não encontrado.");
    }
}
