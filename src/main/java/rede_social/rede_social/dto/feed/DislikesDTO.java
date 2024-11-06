package rede_social.rede_social.dto.feed;

import java.util.List;

public record DislikesDTO(Long postId, Long userId, List<ListDislikeDTO> dislikes, Number totalDislikes) {
}
