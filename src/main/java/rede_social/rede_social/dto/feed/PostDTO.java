package rede_social.rede_social.dto.feed;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Base64;
import java.util.List;

public record PostDTO(
        Long id,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("name") String name,
        @JsonProperty("photo")  String photo,
        String content,
        int dislikes,
        String createdAt,
        List<LikeDTO> likes,
        List<CommentDTO> comments
) {

}