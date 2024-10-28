package rede_social.rede_social.dto.feed;

import java.util.List;

public record FeedTopDTO(List<TopPostDTO> topPosts, String message) {
    public FeedTopDTO {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
    }
}
