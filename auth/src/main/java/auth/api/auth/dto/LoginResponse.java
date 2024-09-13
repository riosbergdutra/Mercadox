package auth.api.auth.dto;

public record LoginResponse(String accessToken,String refreshToken) {
}