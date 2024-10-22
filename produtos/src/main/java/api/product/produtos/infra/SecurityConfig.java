package api.product.produtos.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produtos/findall").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produtos/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produtos/{idProduto}/verificar-estoque/{quantidade}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/produtos/{idVendedor}/criarproduto").hasAuthority("SCOPE_VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/{idVendedor}/atualizarproduto/{idProduto}").hasAuthority("SCOPE_VENDEDOR")
                        .requestMatchers(HttpMethod.DELETE, "/{idVendedor}/deletarproduto/{idProduto}").hasAuthority("SCOPE_VENDEDOR")
                        
                        // Regras para avaliações
                        .requestMatchers(HttpMethod.POST, "/avaliacoes/{idUsuario}/criar").hasAuthority("SCOPE_USUARIO")
                        .requestMatchers(HttpMethod.PUT, "/avaliacoes/{idUsuario}/{idAvaliacao}/atualizar").hasAuthority("SCOPE_USUARIO")
                        .requestMatchers(HttpMethod.DELETE, "/avaliacoes/{idUsuario}/{idAvaliacao}/deletar").hasAuthority("SCOPE_USUARIO")
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/produto/{idProduto}").permitAll() 
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/{idAvaliacao}").permitAll() 
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/media/{idProduto}").permitAll() 
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
