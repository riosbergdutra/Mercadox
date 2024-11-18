package user.api.usuario.usuario.infra;

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

/**
 * Configuração de segurança da aplicação.
 * 
 * Esta classe configura a segurança da aplicação utilizando Spring Security,
 * incluindo a configuração de autenticação com JWT,
 * controle de acesso, suporte a CORS e criptografia de senhas.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    /**
     * Configura o filtro de segurança da aplicação.
     * 
     * @param http Configuração de segurança HTTP.
     * @return Configuração do filtro de segurança.
     * @throws Exception Se houver algum problema ao configurar a segurança.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                       // Endpoints do actuator e Swagger
                       .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                       .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                       
                       // Endpoints do usuário
                       .requestMatchers(HttpMethod.POST, "/usuario/criar").permitAll()
                       .requestMatchers(HttpMethod.GET, "/usuario/{id}").authenticated()
                       .requestMatchers(HttpMethod.GET, "/usuario/email/").permitAll()
                       .requestMatchers(HttpMethod.PUT, "/usuario/senha/{id}").authenticated()
                       .requestMatchers(HttpMethod.DELETE, "/usuario/{id}").authenticated()
                       .requestMatchers(HttpMethod.GET, "/usuario/token/**").permitAll()

                       // Endpoints de endereço
                       .requestMatchers(HttpMethod.POST, "/endereco/{userId}/criar").authenticated()
                       .requestMatchers(HttpMethod.GET, "/endereco/{userId}/{idEndereco}").authenticated()
                       .requestMatchers(HttpMethod.PUT, "/endereco/{userId}/{idEndereco}").authenticated()
                       .requestMatchers(HttpMethod.DELETE, "/endereco/{userId}/{idEndereco}").authenticated()
                       .requestMatchers(HttpMethod.GET, "/endereco/{userId}").authenticated()

                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /**
     * Configura o decodificador de JWT utilizando a chave pública fornecida.
     * 
     * @return Decodificador JWT.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Configura o suporte a CORS na aplicação.
     * 
     * @return Configuração do WebMvcConfigurer para CORS.
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*") // Permite todas as origens
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true); // Permite credenciais (cookies)
            }
        };
    }

    /**
     * Configura o encoder de senha BCrypt.
     * 
     * @return Encoder de senha BCrypt.
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
