package com.umc.movie.dto.response;

public record LoginResponse(
        String nickname,
        String accessToken
) {
}
