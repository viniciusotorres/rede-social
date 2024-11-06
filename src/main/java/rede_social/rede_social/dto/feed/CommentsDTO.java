package rede_social.rede_social.dto.feed;

import java.util.List;

public record CommentsDTO(Long postId, Long userId, List<ListCommentDTO> comments, Number countComments) {
}
