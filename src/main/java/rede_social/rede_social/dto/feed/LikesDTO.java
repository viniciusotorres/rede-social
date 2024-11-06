package rede_social.rede_social.dto.feed;

import java.util.ArrayList;
import java.util.List;

public record LikesDTO(Long postId, Long userId, List<ListLikeDTO> likes, Number countLikes) {

}
