package auth.api.auth.dto;

public record LoginResponse(String token, long expiresIn) {
}