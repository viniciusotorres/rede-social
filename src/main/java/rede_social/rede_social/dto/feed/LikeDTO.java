package rede_social.rede_social.dto.feed;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LikeDTO(
        Long id,
        @JsonProperty("user_id") Long userId
) {}