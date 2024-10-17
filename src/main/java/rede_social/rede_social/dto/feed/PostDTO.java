package rede_social.rede_social.dto.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PostDTO(
        Long id,
        @JsonProperty("user_id") Long userId,
        String content,
        int dislikes,
        String createdAt,
        List<LikeDTO> likes,
        List<CommentDTO> comments
) {}