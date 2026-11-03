package com.umc.movie.dto.response;

public record MemberResponse(
        Long memberId,
        String email,
        String nickname
) {}