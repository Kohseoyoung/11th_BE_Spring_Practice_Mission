package com.umc.movie.dto.response;

public record MovieRecordResponse(
        RatingResponse rating,
        FavoriteResponse favorite
) {
}