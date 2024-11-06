package rede_social.rede_social.dto.feed;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DislikeDTO(
        Long id,
        @JsonProperty("user_id") Long userId,
        Long postId
) {
}
