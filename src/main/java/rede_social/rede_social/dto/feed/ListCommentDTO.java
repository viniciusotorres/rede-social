package rede_social.rede_social.dto.feed;

public record ListCommentDTO(Long commentId,Long userId, String username, String content, String date) {
}
