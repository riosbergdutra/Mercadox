package auth.api.auth.dto;

public record LoginResponse(String token,String refreshToken, long expiresIn) {
}