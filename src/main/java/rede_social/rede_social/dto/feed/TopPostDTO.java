package rede_social.rede_social.dto.feed;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TopPostDTO(
        Long id,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("name") String name,
        String content,
        String createdAt,
        List<LikeDTO> likes,
        List<CommentDTO> comments
) {
}
